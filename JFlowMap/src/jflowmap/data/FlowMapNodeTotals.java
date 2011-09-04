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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jflowmap.FlowDirection;
import jflowmap.FlowEndpoint;
import jflowmap.FlowMapGraph;
import jflowmap.FlowMapGraphSet;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

/**
 * @author Ilya Boyandin
 */
public class FlowMapNodeTotals {

  // TODO: generalize FlowMapSummaries (shouldn't be only for regions)
//  public static final String NODE_COLUMN__SUM_OUTGOING_DIFF_TO_NEXT_YEAR = "sumOutDiff:stat";
//  public static final String NODE_COLUMN__SUM_INCOMING_DIFF_TO_NEXT_YEAR = "sumIncDiff:stat";

  public static final String NODE_COLUMN__SUM_OUTGOING_INTRAREG = "outIntra:stat:";
  public static final String NODE_COLUMN__SUM_INCOMING_INTRAREG = "inIntra:stat:";

  private static final String NODE_COLUMN__SUM_OUTGOING_PREFIX = "sumOut:stat:";
  private static final String NODE_COLUMN__SUM_INCOMING_PREFIX= "sumIn:stat:";


  private FlowMapNodeTotals() {
  }

  public static double getTotalWeight(Node node, String weightAttrName, FlowDirection dir) {
    String attrName = getTotalWeightNodeAttr(weightAttrName, dir);
    if (!node.canGet(attrName, double.class)) {
      throw new IllegalArgumentException("Cannot get node totals attr: " + attrName);
    }
    return node.getDouble(attrName);
  }

  public static void supplyNodesWithWeightTotals(FlowMapGraphSet flowMapGraphSet) {
    for (FlowMapGraph flowMapGraph : flowMapGraphSet.asList()) {
      supplyNodesWithWeightTotals(flowMapGraph);
    }
  }

  /**
   * This method adds additional columns to the nodes table providing
   * the nodes with useful stats.
   */
  public static void supplyNodesWithWeightTotals(FlowMapGraph flowMapGraph) {
    supplyNodesWithWeightTotals(flowMapGraph, flowMapGraph.getEdgeWeightAttrs());
  }

  public static void supplyNodesWithWeightTotals(FlowMapGraph flowMapGraph, List<String> attrNames) {
    for (String weightAttrName : attrNames) {
      supplyNodesWithWeightTotals(flowMapGraph, weightAttrName);
    }
  }


  public static String getTotalWeightNodeAttr(String weightAttrName, FlowDirection dir) {
    switch (dir) {
    case OUTGOING: return NODE_COLUMN__SUM_OUTGOING_PREFIX + weightAttrName;
    case INCOMING: return NODE_COLUMN__SUM_INCOMING_PREFIX + weightAttrName;
    default: throw new IllegalArgumentException();
    }
  }


  /**
   * Returns map nodeId->total value
   */
  public static Map<String, Double> calcNodeTotalsFor(
      FlowMapGraph fmg, Iterable<Edge> edges, String attrName, FlowEndpoint ep) {

    HashMap<String, Double> map = Maps.newHashMap();
    for (Edge e : edges) {
      String nodeId = fmg.getNodeId(ep.nodeOf(e));
      double val = fmg.getEdgeWeight(e, attrName);
      Double prevSum = map.get(nodeId);
      if (prevSum == null  ||  Double.isNaN(prevSum)) {
        map.put(nodeId, val);
      } else {
        if (!Double.isNaN(val)) {
          map.put(nodeId, prevSum + val);
        }
      }
    }
    return map;
  }



  private static void supplyNodesWithWeightTotals(FlowMapGraph fmg, String weightAttrName) {
    Graph g = fmg.getGraph();
    Table nodeTable = g.getNodeTable();

    Map<Integer, Double> outsums = Maps.newHashMap();
    Map<Integer, Double> insums = Maps.newHashMap();

    for (Edge e : fmg.edges()) {

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

    String outgoingSumAttrName = getTotalWeightNodeAttr(weightAttrName, FlowDirection.OUTGOING);
    String incomingSumAttrName = getTotalWeightNodeAttr(weightAttrName, FlowDirection.INCOMING);

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

  public static void supplyNodesWithIntraregTotals(FlowMapGraphSet fmset, String nodeRegionAttr,
      String edgeWeightAttr) {
    for (FlowMapGraph fmg : fmset.asList()) {
      FlowMapNodeTotals.supplyNodesWithIntraregTotals(fmg, nodeRegionAttr, edgeWeightAttr);
    }
  }

  public static void supplyNodesWithIntraregTotals(FlowMapGraph flowMapGraph,
      String nodeRegionAttr, String edgeWeightAttr) {
    Graph g = flowMapGraph.getGraph();
    Table nodeTable = g.getNodeTable();

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

    nodeTable.addColumn(FlowMapNodeTotals.NODE_COLUMN__SUM_OUTGOING_INTRAREG, double.class);
    nodeTable.addColumn(FlowMapNodeTotals.NODE_COLUMN__SUM_INCOMING_INTRAREG, double.class);
    for (int i = 0, numNodes = g.getNodeCount(); i < numNodes; i++) {
      Node node = g.getNode(i);
      if (outsums.containsKey(i)) {
        node.setDouble(FlowMapNodeTotals.NODE_COLUMN__SUM_OUTGOING_INTRAREG, outsums.get(i));
      }
      if (insums.containsKey(i)) {
        node.setDouble(FlowMapNodeTotals.NODE_COLUMN__SUM_INCOMING_INTRAREG, insums.get(i));
      }
    }
  }

  public static Iterable<String> getWeightTotalsNodeAttrs(
      Iterable<String> edgeWeightAttrNames, FlowDirection dir) {
    List<String> list = new ArrayList<String>(Iterables.size(edgeWeightAttrNames));
    for (String attr : edgeWeightAttrNames) {
      list.add(getTotalWeightNodeAttr(attr, dir));
    }
    return list;
  }

  public static Comparator<Node> createMaxNodeWeightTotalsComparator(FlowMapGraph fmg,
      FlowDirection dir) {
    List<String> weightAttrs = fmg.getEdgeWeightAttrs();
    Comparator<Node> comp = fmg.createMaxNodeAttrValueComparator(
          getWeightTotalsNodeAttrs(weightAttrs, dir)
    );
    return comp;
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
