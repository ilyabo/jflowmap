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

import java.util.Map;

import jflowmap.EdgeDirection;
import jflowmap.FlowMapAttrSpec;
import jflowmap.FlowMapGraph;
import jflowmap.FlowMapGraphSet;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;

import com.google.common.collect.Maps;

/**
 * @author Ilya Boyandin
 */
public class FlowMapSummaries {

  // TODO: generalize FlowMapSummaries (shouldn't be only for regions)
//  public static final String NODE_COLUMN__SUM_OUTGOING_DIFF_TO_NEXT_YEAR = "sumOutDiff:stat";
//  public static final String NODE_COLUMN__SUM_INCOMING_DIFF_TO_NEXT_YEAR = "sumIncDiff:stat";

  public static final String NODE_COLUMN__SUM_OUTGOING_INTRAREG = "outIntra:stat";
  public static final String NODE_COLUMN__SUM_INCOMING_INTRAREG = "inIntra:stat";

  private static final String NODE_COLUMN__SUM_OUTGOING = "sumOut:stat";
  private static final String NODE_COLUMN__SUM_INCOMING= "sumIn:stat";


  private FlowMapSummaries() {
  }

  public static double getWeightSummary(Node node, String weightAttrName, EdgeDirection dir) {
    return node.getDouble(getWeightSummaryNodeAttr(weightAttrName, dir));
  }

  public static void supplyNodesWithWeightSummaries(FlowMapGraphSet flowMapGraphSet) {
    for (FlowMapGraph flowMapGraph : flowMapGraphSet.asList()) {
      FlowMapSummaries.supplyNodesWithWeightSummaries(flowMapGraph);
    }
  }

  /**
   * This method adds additional columns to the nodes table providing
   * the nodes with useful stats.
   */
  public static void supplyNodesWithWeightSummaries(FlowMapGraph flowMapGraph) {
    for (String weightAttrName : flowMapGraph.getEdgeWeightAttrNames()) {
      supplyNodesWithWeightSummaries(flowMapGraph, weightAttrName);
    }
  }

  public static String getWeightSummaryNodeAttr(String weightAttrName, EdgeDirection dir) {
    switch (dir) {
    case OUTGOING: return NODE_COLUMN__SUM_OUTGOING + weightAttrName;
    case INCOMING: return NODE_COLUMN__SUM_INCOMING + weightAttrName;
    default: throw new IllegalArgumentException();
    }
  }

  private static void supplyNodesWithWeightSummaries(FlowMapGraph flowMapGraph,
      String weightAttrName) {
    Graph g = flowMapGraph.getGraph();
    Table nodeTable = g.getNodeTable();

    Map<Integer, Double> outsums = Maps.newHashMap();
    Map<Integer, Double> insums = Maps.newHashMap();

    for (int i = 0, numEdges = g.getEdgeCount(); i < numEdges; i++) {
      Edge e = g.getEdge(i);

      double v = e.getDouble(weightAttrName);
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
      }
    }

    String outgoingSumAttrName = getWeightSummaryNodeAttr(weightAttrName, EdgeDirection.OUTGOING);
    String incomingSumAttrName = getWeightSummaryNodeAttr(weightAttrName, EdgeDirection.INCOMING);

    nodeTable.addColumn(outgoingSumAttrName, double.class);
    nodeTable.addColumn(incomingSumAttrName, double.class);

    for (int i = 0, numNodes = g.getNodeCount(); i < numNodes; i++) {
      Node node = g.getNode(i);
      if (outsums.containsKey(i)) {
        node.setDouble(outgoingSumAttrName, outsums.get(i));
      }
      if (insums.containsKey(i)) {
        node.setDouble(incomingSumAttrName, insums.get(i));
      }
    }
  }





  // TODO: fix intrareg summaries to support wildcarded weight attrs

  public static void supplyNodesWithIntraregSummaries(FlowMapGraphSet fmset, String nodeRegionAttr,
      String edgeWeightAttr) {
    for (FlowMapGraph fmg : fmset.asList()) {
      FlowMapSummaries.supplyNodesWithIntraregSummaries(fmg, nodeRegionAttr, edgeWeightAttr);
    }
  }

  public static void supplyNodesWithIntraregSummaries(FlowMapGraph flowMapGraph,
      String nodeRegionAttr, String edgeWeightAttr) {
    Graph g = flowMapGraph.getGraph();
    Table nodeTable = g.getNodeTable();
    FlowMapAttrSpec as = flowMapGraph.getAttrSpec();

    Map<Integer, Double> outsums = Maps.newHashMap();
    Map<Integer, Double> insums = Maps.newHashMap();

    for (int i = 0, numEdges = g.getEdgeCount(); i < numEdges; i++) {
      Edge e = g.getEdge(i);

      double w = e.getDouble(edgeWeightAttr);
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
  /*
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
  */

  /**
   * Find max outgoing or incoming summary weight for every node over the set.
   * @param flowMapGraphSet Must be supplied with summaries
   * (invoke {@link#supplyNodesWithWeightSummaries(FlowMapGraphSet)} first)
   * @return Map: (nodeId -> maxWeight)
   */
  /*
  public static Map<String, Double> findMaxWeightForEachNode(FlowMapGraphSet flowMapGraphSet,
      boolean incomingNotOutgoing) {
    Map<String, Double> map = Maps.newHashMap();
    for (FlowMapGraph fmg : flowMapGraphSet.asList()) {
      for (Node node : fmg.nodes()) {
        double sumW = node.getDouble(incomingNotOutgoing ?
            FlowMapSummaries.NODE_COLUMN__SUM_INCOMING :
              FlowMapSummaries.NODE_COLUMN__SUM_OUTGOING);
        if (!Double.isNaN(sumW)) {
          String nodeId = FlowMapGraph.getNodeId(node);
          Double max = map.get(nodeId);
          if (max == null || max.isNaN() || sumW > max.doubleValue()) {
            map.put(nodeId, sumW);
          }
        }
      }
    }
    return map;
  }
  */

}
