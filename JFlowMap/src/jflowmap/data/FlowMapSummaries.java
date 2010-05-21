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

import java.util.Map;

import jflowmap.FlowMapGraph;
import jflowmap.FlowMapAttrSpec;
import jflowmap.FlowMapGraphWithAttrSpecs;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;

import com.google.common.collect.Maps;

/**
 * @author Ilya Boyandin
 */
public class FlowMapSummaries {

    public static final String NODE_COLUMN__SUM_OUTGOING_DIFF_TO_NEXT_YEAR = "sumOutDiff:stat";
    public static final String NODE_COLUMN__SUM_INCOMING_DIFF_TO_NEXT_YEAR = "sumIncDiff:stat";

    public static final String NODE_COLUMN__SUM_OUTGOING_INTRAREG = "outIntra:stat";
    public static final String NODE_COLUMN__SUM_INCOMING_INTRAREG = "inIntra:stat";

    public static final String NODE_COLUMN__SUM_OUTGOING = "sumOut:stat";
    public static final String NODE_COLUMN__SUM_INCOMING= "sumIn:stat";


    private FlowMapSummaries() {
    }

    /**
     * This method adds additional columns to the nodes table providing
     * the nodes with useful stats.
     */
    public static FlowMapGraphWithAttrSpecs supplyNodesWithSummaries(FlowMapGraphWithAttrSpecs graphAndSpecs) {
        supplyNodesWithSummaries(graphAndSpecs.getGraph(), graphAndSpecs.getAttrsSpec());
        return graphAndSpecs;
    }

    public static void supplyNodesWithSummaries(Graph g, FlowMapAttrSpec as) {
        Table nodeTable = g.getNodeTable();

        Map<Integer, Double> outsums = Maps.newHashMap();
        Map<Integer, Double> insums = Maps.newHashMap();

        for (int i = 0, numEdges = g.getEdgeCount(); i < numEdges; i++) {
            Edge e = g.getEdge(i);

            double v = e.getDouble(as.getEdgeWeightAttr());
            if (!Double.isNaN(v)) {
                Node src = e.getSourceNode();
                Node trg = e.getTargetNode();
                int srcRow = src.getRow();
                int trgRow = trg.getRow();

                Double outsum = outsums.get(srcRow);
                if (outsum == null) {
                    outsums.put(srcRow, v);
                } else {
                    outsums.put(srcRow, outsum + v);
                }

                Double inval = insums.get(trgRow);
                if (inval == null) {
                    insums.put(trgRow, v);
                } else {
                    insums.put(trgRow, inval + v);
                }
//                if (FlowMapLoader.getGraphId(g).equals("1992")) {
//                    if (trg.getString("name").equals("Asia | Southern Asia")) {
//                        System.out.println(src.getString("name") + " -> " + trg.getString("name")  + " (" + v
//                                + "), Sum: " + insums.get(trgRow));
//                    }
//                }
            }
        }


        nodeTable.addColumn(FlowMapSummaries.NODE_COLUMN__SUM_OUTGOING, double.class);
        nodeTable.addColumn(FlowMapSummaries.NODE_COLUMN__SUM_INCOMING, double.class);
        for (int i = 0, numNodes = g.getNodeCount(); i < numNodes; i++) {
            Node node = g.getNode(i);
            if (outsums.containsKey(i)) {
                node.setDouble(FlowMapSummaries.NODE_COLUMN__SUM_OUTGOING, outsums.get(i));
            }
            if (insums.containsKey(i)) {
                node.setDouble(FlowMapSummaries.NODE_COLUMN__SUM_INCOMING, insums.get(i));
            }
        }
    }

    public static void supplyNodesWithIntraregSummaries(FlowMapGraphWithAttrSpecs graphAndSpecs, String nodeRegionAttr) {
        Graph g = graphAndSpecs.getGraph();
        Table nodeTable = g.getNodeTable();
        FlowMapAttrSpec as = graphAndSpecs.getAttrsSpec();

        Map<Integer, Double> outsums = Maps.newHashMap();
        Map<Integer, Double> insums = Maps.newHashMap();

        for (int i = 0, numEdges = g.getEdgeCount(); i < numEdges; i++) {
            Edge e = g.getEdge(i);

            double w = e.getDouble(as.getEdgeWeightAttr());
            if (!Double.isNaN(w)) {
                Node src = e.getSourceNode();
                Node trg = e.getTargetNode();
                int srcRow = src.getRow();
                int trgRow = trg.getRow();

                String srcRegion = src.getString(nodeRegionAttr);
                String trgRegion = trg.getString(nodeRegionAttr);

                if (srcRegion.equals(trgRegion)) { // if it's local add to the sums
                    Double outsum = outsums.get(srcRow);
                    if (outsum == null) {
                        outsums.put(srcRow, w);
                    } else {
                        outsums.put(srcRow, outsum + w);
                    }

                    Double insum = insums.get(trgRow);
                    if (insum == null) {
                        insums.put(trgRow, w);
                    } else {
                        insums.put(trgRow, insum + w);
                    }
                }
            }
        }

        nodeTable.addColumn(FlowMapSummaries.NODE_COLUMN__SUM_OUTGOING_INTRAREG, double.class);
        nodeTable.addColumn(FlowMapSummaries.NODE_COLUMN__SUM_INCOMING_INTRAREG, double.class);
        for (int i = 0, numNodes = g.getNodeCount(); i < numNodes; i++) {
            Node node = g.getNode(i);
            if (outsums.containsKey(i)) {
                node.setDouble(FlowMapSummaries.NODE_COLUMN__SUM_OUTGOING_INTRAREG, outsums.get(i));
            }
            if (insums.containsKey(i)) {
                node.setDouble(FlowMapSummaries.NODE_COLUMN__SUM_INCOMING_INTRAREG, insums.get(i));
            }
        }
    }

    /**
     * This method requires that supplyNodesWithSummaries was already called for the graphs
     */
    public static void supplyNodesWithDiffs(Iterable<Graph> graphs, FlowMapAttrSpec attrSpec) {
        Graph prevg = null;
        for (Graph g : graphs) {
            g.addColumn(FlowMapSummaries.NODE_COLUMN__SUM_INCOMING_DIFF_TO_NEXT_YEAR, double.class);
            g.addColumn(FlowMapSummaries.NODE_COLUMN__SUM_OUTGOING_DIFF_TO_NEXT_YEAR, double.class);
            for (int i = 0, numNodes = g.getNodeCount(); i < numNodes; i++) {
                Node node = g.getNode(i);
                String nodeId = FlowMapGraph.getNodeId(node);
                Node prevNode = null;
                if (prevg != null) {
                    prevNode = FlowMapGraph.findNodeById(prevg, nodeId);
                }

                double diffIn, diffOut;
                if (prevNode == null) {
                    diffIn = diffOut = Double.NaN;
                } else {
                    diffIn = diff(
                                node.getDouble(FlowMapSummaries.NODE_COLUMN__SUM_INCOMING),
                                prevNode.getDouble(FlowMapSummaries.NODE_COLUMN__SUM_INCOMING));
                    diffOut = diff(
                                node.getDouble(FlowMapSummaries.NODE_COLUMN__SUM_OUTGOING),
                                prevNode.getDouble(FlowMapSummaries.NODE_COLUMN__SUM_OUTGOING));
                }

                node.setDouble(FlowMapSummaries.NODE_COLUMN__SUM_INCOMING_DIFF_TO_NEXT_YEAR, diffIn);
                node.setDouble(FlowMapSummaries.NODE_COLUMN__SUM_OUTGOING_DIFF_TO_NEXT_YEAR, diffOut);
            }
            prevg = g;
        }
    }



    private static double diff(double current, double prev) {
        return (current - prev);
    }

    private static double diffPercentage(double current, double prev) {
        return (current - prev)/prev;  // TODO: diffPercentage (ensure it works properly; check for division by zero)
    }

}
