/*
 * This file is part of JFlowMap.
 *
 * Copyright 2009 Ilya Boyandin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jflowmap.data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jflowmap.FlowMapAttrSpec;
import jflowmap.FlowMapGraph;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.tuple.TupleSet;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

/**
 * @author Ilya Boyandin
 */
public class FlowMapStats {

  private static final String NODE_ATTR_KEY_PREFIX = "NODE_";
  private static final String EDGE_ATTR_KEY_PREFIX = "EDGE_";

  private final Map<String, MinMax> statsCache = new HashMap<String, MinMax>();
  private final List<FlowMapGraph> flowMapGraphs;

  private FlowMapStats(List<FlowMapGraph> graphAndSpecs) {
    this.flowMapGraphs = graphAndSpecs;
    // TODO: add property change listeners
  }

  public static FlowMapStats createFor(FlowMapGraph flowMapGraph) {
    return new FlowMapStats(Arrays.asList(flowMapGraph));
  }

  public static FlowMapStats createFor(Iterable<Graph> graphs, final FlowMapAttrSpec attrSpecs) {
    return createFor(Iterables.transform(graphs, new Function<Graph, FlowMapGraph>() {
      @Override
      public FlowMapGraph apply(Graph from) {
        return new FlowMapGraph(from, attrSpecs);
      }
    }));
  }

  public static FlowMapStats createFor(Iterable<FlowMapGraph> flowMapGraphs) {
    return new FlowMapStats(ImmutableList.copyOf(flowMapGraphs));
  }

  public MinMax getEdgeLengthStats() {
    return getCachedOrCalc(
        EDGE_ATTR_KEY_PREFIX + "LENGTH",
        new AttrStatsCalculator() {
          @Override
          public MinMax calc() {
            List<Double> edgeLengths = Lists.newArrayList();
            for (FlowMapGraph fmm : flowMapGraphs) {
              Graph graph = fmm.getGraph();
              for (int i = 0, size = graph.getEdgeCount(); i < size; i++) {
                Edge edge = graph.getEdge(i);
                Node src = edge.getSourceNode();
                Node target = edge.getTargetNode();
                double x1 = src.getDouble(fmm.getXNodeAttr());
                double y1 = src.getDouble(fmm.getYNodeAttr());
                double x2 = target.getDouble(fmm.getXNodeAttr());
                double y2 = target.getDouble(fmm.getYNodeAttr());
                edgeLengths.add(Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2)));
              }
            }
            return MinMax.createFor(edgeLengths.iterator());
          }
        });
  }


  public MinMax getEdgeWeightStats() {
    return getEdgeAttrsStats(EDGE_ATTR_KEY_PREFIX + "WEIGHT",
        new Function<FlowMapGraph, List<String>>() {
          @Override
          public List<String> apply(FlowMapGraph fmg) {
            return fmg.getEdgeWeightAttrNames();
          }
        }
    );
  }

  private MinMax getEdgeAttrsStats(String key, final Function<FlowMapGraph, List<String>> getEdgeAttrs) {
    return getCachedOrCalc(
        key,
        new AttrStatsCalculator() {
          @Override public MinMax calc() {
            MinMax minMax = null;
            for (FlowMapGraph fmg : flowMapGraphs) {
              MinMax mm = TupleStats.createFor(fmg.getGraph().getEdges(), getEdgeAttrs.apply(fmg));
              if (minMax == null) {
                minMax = mm;
              } else {
                minMax = minMax.mergeWith(mm);
              }
            }
            return minMax;
          }
        }
    );
  }

  public MinMax getEdgeWeightDiffStats() {
    return getEdgeAttrsStats(EDGE_ATTR_KEY_PREFIX + "WEIGHT_DIFF",
        new Function<FlowMapGraph, List<String>>() {
          @Override
          public List<String> apply(FlowMapGraph fmg) {
            return fmg.getEdgeWeightDiffAttrNames();
          }
        }
    );
  }


  public MinMax getEdgeWeightRelativeDiffStats() {
    return getEdgeAttrsStats(EDGE_ATTR_KEY_PREFIX + "WEIGHT_DIFF_REL",
        new Function<FlowMapGraph, List<String>>() {
          @Override
          public List<String> apply(FlowMapGraph fmg) {
            return fmg.getEdgeWeightRelativeDiffAttrNames();
          }
        }
    );
  }



  public MinMax getNodeXStats() {
    return getAttrStats(Attrs.NODE_X);
  }

  public MinMax getNodeYStats() {
    return getAttrStats(Attrs.NODE_Y);
  }

  /**
   * NOTE: this method suggests that every graph in this flowMapGraphs
   * has the attribute with <code>attrName</code>.
   */
  public MinMax getNodeAttrStats(final String attrName) {
    return getCachedOrCalc(NODE_ATTR_KEY_PREFIX + attrName, new AttrStatsCalculator() {
      @Override
      public MinMax calc() {
        return TupleStats.createFor(
            attrIterator(Tuples.NODES),
            attrIterator(new Function<FlowMapGraph, String>() {
              @Override
              public String apply(FlowMapGraph from) {
                return attrName;
              }
            }));
      }
    });
  }

  private MinMax getAttrStats(final Attrs attr) {
    return getCachedOrCalc(attr.name(), new AttrStatsCalculator() {
      @Override
      public MinMax calc() {
        return TupleStats.createFor(
            attrIterator(attr.funToTupleSet()), attrIterator(attr.funToName()));
      }
    });
  }

  private synchronized MinMax getCachedOrCalc(String key, AttrStatsCalculator calc) {
    MinMax stats = statsCache.get(key);
    if (stats == null) {
      stats = calc.calc();
      statsCache.put(key, stats);
    }
    return stats;
  }

  @SuppressWarnings("unchecked")
  private <T> Iterator<T> attrIterator(Function<FlowMapGraph, T> function) {
    return Iterators.concat(Iterators.transform(flowMapGraphs.iterator(), function));
  }

  private interface AttrStatsCalculator {
    MinMax calc();
  }

  private enum Tuples implements Function<FlowMapGraph, TupleSet> {
    EDGES {
      public TupleSet apply(FlowMapGraph from) {
        return from.getGraph().getEdges();
      }
    },
    NODES {
      public TupleSet apply(FlowMapGraph from) {
        return from.getGraph().getNodes();
      }
    },
    ;
  };

  private enum Attrs  {
    NODE_X {
      @Override
      public Function<FlowMapGraph, String> funToName() {
        return Name.NODE_X;
      }
      @Override
      public Function<FlowMapGraph, TupleSet> funToTupleSet() {
        return Tuples.NODES;
      }
    },
    NODE_Y {
      @Override
      public Function<FlowMapGraph, String> funToName() {
        return Name.NODE_Y;
      }
      @Override
      public Function<FlowMapGraph, TupleSet> funToTupleSet() {
        return Tuples.NODES;
      }
    },
    ;

    public abstract Function<FlowMapGraph, String> funToName();

    public abstract Function<FlowMapGraph, TupleSet> funToTupleSet();

    private enum Name implements Function<FlowMapGraph, String> {
      NODE_X {
        public String apply(FlowMapGraph from) {
          return from.getXNodeAttr();
        }
      },
      NODE_Y {
        public String apply(FlowMapGraph from) {
          return from.getYNodeAttr();
        }
      },
      ;
    };
  }


}
