package jflowmap.data;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import jflowmap.FlowMapAttrSpec;
import jflowmap.FlowMapGraph;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.tuple.TupleSet;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

/**
 * @author Ilya Boyandin
 * @deprecated
 */
@Deprecated
public class MultiFlowMapStats extends AbstractFlowMapStats {

  private final List<FlowMapGraph> flowMapGraphs;

  private MultiFlowMapStats(List<FlowMapGraph> flowMapGraphs) {
    super(flowMapGraphs.get(0).getAttrSpec());
    this.flowMapGraphs = flowMapGraphs;
    // TODO: add property change listeners
  }

  @Override
  protected Iterable<Edge> edges() {
    return Iterables.concat(Iterables.transform(
        flowMapGraphs,
        new Function<FlowMapGraph, Iterable<Edge>>() { @Override
        public Iterable<Edge> apply(FlowMapGraph fmg) {
          return fmg.edges();
        }
      }));
  }

  public static FlowMapStats createFor(FlowMapGraph flowMapGraph) {
    return new MultiFlowMapStats(Arrays.asList(flowMapGraph));
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
    return new MultiFlowMapStats(ImmutableList.copyOf(flowMapGraphs));
  }


  @Override
  public SeqStat getEdgeWeightAttrStats(String weightAttr) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SeqStat getEdgeWeightStats() {
    return getEdgeAttrsStats(AttrKeys.EDGE_WEIGHT.name(),
        new Function<FlowMapGraph, List<String>>() {
          @Override
          public List<String> apply(FlowMapGraph fmg) {
            return fmg.getEdgeWeightAttrs();
          }
        }
    );
  }

  protected SeqStat getEdgeAttrsStats(String key, final Function<FlowMapGraph, List<String>> getEdgeAttrs) {
    return getCachedOrCalc(
        key,
        new AttrStatsCalculator() {
          @Override public SeqStat calc() {
            SeqStat minMax = null;
            for (FlowMapGraph fmg : flowMapGraphs) {
              SeqStat mm = TupleStats.createFor(fmg.getGraph().getEdges(), getEdgeAttrs.apply(fmg));
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

  public SeqStat getEdgeWeightDiffStats() {
    return getEdgeAttrsStats(AttrKeys.EDGE_WEIGHT_DIFF.name(),
        new Function<FlowMapGraph, List<String>>() {
          @Override
          public List<String> apply(FlowMapGraph fmg) {
            return fmg.getEdgeWeightDiffAttr();
          }
        }
    );
  }


  public SeqStat getEdgeWeightRelativeDiffStats() {
    return getEdgeAttrsStats(AttrKeys.EDGE_WEIGHT_DIFF_REL.name(),
        new Function<FlowMapGraph, List<String>>() {
          @Override
          public List<String> apply(FlowMapGraph fmg) {
            return fmg.getEdgeWeightRelativeDiffAttrNames();
          }
        }
    );
  }



  public SeqStat getNodeXStats() {
    return getAttrStats(Attrs.NODE_X);
  }

  public SeqStat getNodeYStats() {
    return getAttrStats(Attrs.NODE_Y);
  }

  /**
   * NOTE: this method suggests that every graph in this flowMapGraphs
   * has the attribute with <code>attrName</code>.
   */
  public SeqStat getNodeAttrStats(final String attrName) {
    return getCachedOrCalc(AttrKeys.nodeAttr(attrName), new AttrStatsCalculator() {
      @Override
      public SeqStat calc() {
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

  private SeqStat getAttrStats(final Attrs attr) {
    return getCachedOrCalc(attr.name(), new AttrStatsCalculator() {
      @Override
      public SeqStat calc() {
        return TupleStats.createFor(
            attrIterator(attr.funToTupleSet()), attrIterator(attr.funToName()));
      }
    });
  }

  @SuppressWarnings("unchecked")
  private <T> Iterator<T> attrIterator(Function<FlowMapGraph, T> function) {
    return Iterators.concat(Iterators.transform(flowMapGraphs.iterator(), function));
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
          return from.getNodeLonAttr();
        }
      },
      NODE_Y {
        public String apply(FlowMapGraph from) {
          return from.getNodeLatAttr();
        }
      },
      ;
    };
  }

}
