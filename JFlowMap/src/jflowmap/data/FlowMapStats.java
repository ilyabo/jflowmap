/*
 * This file is part of JFlowMap.
 *
 * Copyright 2009 Ilya Boyandin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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

import jflowmap.FlowMapAttrsSpec;
import jflowmap.FlowMapGraphWithAttrSpecs;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.tuple.TupleSet;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @author Ilya Boyandin
 *         Date: 21-Sep-2009
 */
public class FlowMapStats {

    private static final String NODE_ATTR_KEY_SUFFIX = "_nodeAttr";
    private static final String EDGE_ATTR_KEY_SUFFIX = "_edgeAttr";
    public static final String NODE_STATS_COLUMN__SUM_OUTGOING = "sumOutgoing";
    public static final String NODE_STATS_COLUMN__SUM_INCOMING = "sumIncoming";

    private final Map<String, MinMax> statsCache = new HashMap<String, MinMax>();
    private final List<FlowMapGraphWithAttrSpecs> graphAndSpecs;
    private MinMax edgeLengthStats;

    private FlowMapStats(List<FlowMapGraphWithAttrSpecs> graphAndSpecs) {
        this.graphAndSpecs = graphAndSpecs;
        // TODO: add property change listeners
    }

    public static FlowMapStats createFor(FlowMapGraphWithAttrSpecs graphAndSpecs) {
        return new FlowMapStats(Arrays.asList(graphAndSpecs));
    }

    public static FlowMapStats createFor(Iterable<FlowMapGraphWithAttrSpecs> graphAndSpecs) {
        return new FlowMapStats(ImmutableList.copyOf(graphAndSpecs));
    }

    public MinMax getEdgeLengthStats() {
        if (edgeLengthStats == null) {
            List<Double> edgeLengths = Lists.newArrayList();
            for (FlowMapGraphWithAttrSpecs fmm : graphAndSpecs) {
                Graph graph = fmm.getGraph();
                for (int i = 0, size = graph.getEdgeCount(); i < size; i++) {
                    Edge edge = graph.getEdge(i);
                    Node src = edge.getSourceNode();
                    Node target = edge.getTargetNode();
                    double x1 = src.getDouble(fmm.getAttrsSpec().getXNodeAttr());
                    double y1 = src.getDouble(fmm.getAttrsSpec().getYNodeAttr());
                    double x2 = target.getDouble(fmm.getAttrsSpec().getXNodeAttr());
                    double y2 = target.getDouble(fmm.getAttrsSpec().getYNodeAttr());
                    edgeLengths.add(Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2)));
                }
            }
            edgeLengthStats = MinMax.createFor(edgeLengths.iterator());
        }
        return edgeLengthStats;
    }


    public MinMax getEdgeWeightStats() {
        return getAttrStats(Attrs.EDGE_WEIGHT);
    }

    public MinMax getNodeXStats() {
        return getAttrStats(Attrs.NODE_X);
    }

    public MinMax getNodeYStats() {
        return getAttrStats(Attrs.NODE_Y);
    }

    /**
     * NOTE: this method suggests that every graph in this graphAndSpecs
     * has the attribute with <code>attrName</code>.
     */
    public MinMax getNodeAttrStats(final String attrName) {
        String key = attrName + NODE_ATTR_KEY_SUFFIX;
        MinMax stats = statsCache.get(key);
        if (stats == null) {
            stats = TupleStats.createFor(
                    attrIterator(Tuples.NODES),
                    attrIterator(new Function<FlowMapGraphWithAttrSpecs, String>() {
                        @Override
                        public String apply(FlowMapGraphWithAttrSpecs from) {
                            return attrName;
                        }
                    })
            );
            statsCache.put(key, stats);
        }
        return stats;
    }

    public MinMax getAttrStats(Attrs attr) {
    	String key = attr.name();
        MinMax stats = statsCache.get(key);
    	if (stats == null) {
            stats = TupleStats.createFor(
                    attrIterator(attr.funToTupleSet()),
                    attrIterator(attr.funToName())
            );
            statsCache.put(key, stats);
    	}
    	return stats;
    }

    @SuppressWarnings("unchecked")
    private <T> Iterator<T> attrIterator(Function<FlowMapGraphWithAttrSpecs, T> function) {
        return Iterators.concat(Iterators.transform(graphAndSpecs.iterator(), function));
    }

    enum Tuples implements Function<FlowMapGraphWithAttrSpecs, TupleSet> {
        EDGES {
            public TupleSet apply(FlowMapGraphWithAttrSpecs from) {
                return from.getGraph().getEdges();
            }
        },
        NODES {
            public TupleSet apply(FlowMapGraphWithAttrSpecs from) {
                return from.getGraph().getNodes();
            }
        },
        ;
    };

    public enum Attrs  {
        EDGE_WEIGHT {
            @Override
            public Function<FlowMapGraphWithAttrSpecs, String> funToName() {
                return Name.EDGE_WEIGHT;
            }
            @Override
            public Function<FlowMapGraphWithAttrSpecs, TupleSet> funToTupleSet() {
                return Tuples.EDGES;
            }
        },
        NODE_X {
            @Override
            public Function<FlowMapGraphWithAttrSpecs, String> funToName() {
                return Name.NODE_X;
            }
            @Override
            public Function<FlowMapGraphWithAttrSpecs, TupleSet> funToTupleSet() {
                return Tuples.NODES;
            }
        },
        NODE_Y {
            @Override
            public Function<FlowMapGraphWithAttrSpecs, String> funToName() {
                return Name.NODE_Y;
            }
            @Override
            public Function<FlowMapGraphWithAttrSpecs, TupleSet> funToTupleSet() {
                return Tuples.NODES;
            }
        },
        ;

        public abstract Function<FlowMapGraphWithAttrSpecs, String> funToName();

        public abstract Function<FlowMapGraphWithAttrSpecs, TupleSet> funToTupleSet();

        private enum Name implements Function<FlowMapGraphWithAttrSpecs, String> {
            EDGE_WEIGHT {
                public String apply(FlowMapGraphWithAttrSpecs from) {
                    return from.getAttrsSpec().getEdgeWeightAttr();
                }
            },
            NODE_X {
                public String apply(FlowMapGraphWithAttrSpecs from) {
                    return from.getAttrsSpec().getXNodeAttr();
                }
            },
            NODE_Y {
                public String apply(FlowMapGraphWithAttrSpecs from) {
                    return from.getAttrsSpec().getYNodeAttr();
                }
            },
            ;
        };
    }

    /**
     * This method adds additional columns to the nodes table providing
     * the nodes with useful stats.
     */
    public static FlowMapGraphWithAttrSpecs supplyNodesWithStats(FlowMapGraphWithAttrSpecs graphAndSpecs) {

        Graph g = graphAndSpecs.getGraph();
        FlowMapAttrsSpec as = graphAndSpecs.getAttrsSpec();

        Map<Integer, Double> outsums = Maps.newHashMap();
        Map<Integer, Double> insums = Maps.newHashMap();

        for (int i = 0, numEdges = g.getEdgeCount(); i < numEdges; i++) {
            Edge e = g.getEdge(i);

            double w = e.getDouble(as.getEdgeWeightAttr());
            if (!Double.isNaN(w)) {
                int src = e.getSourceNode().getRow();
                int trg = e.getTargetNode().getRow();

                Double outsum = outsums.get(src);
                if (outsum == null) {
                    outsums.put(src, w);
                } else {
                    outsums.put(src, outsum + w);
                }

                Double inval = insums.get(trg);
                if (inval == null) {
                    insums.put(trg, w);
                } else {
                    insums.put(trg, inval + w);
                }
            }
        }


        g.addColumn(NODE_STATS_COLUMN__SUM_OUTGOING, double.class);
        g.addColumn(NODE_STATS_COLUMN__SUM_INCOMING, double.class);
        for (int i = 0, numNodes = g.getNodeCount(); i < numNodes; i++) {
            Node node = g.getNode(i);
            if (outsums.containsKey(i)) {
                node.setDouble(NODE_STATS_COLUMN__SUM_OUTGOING, outsums.get(i));
            }
            if (insums.containsKey(i)) {
                node.setDouble(NODE_STATS_COLUMN__SUM_INCOMING, insums.get(i));
            }
        }

        return graphAndSpecs;
    }
}
