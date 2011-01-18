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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jflowmap.FlowMapAttrSpec;
import jflowmap.FlowMapGraph;
import jflowmap.geom.Point;
import jflowmap.util.ArrayUtils;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;
import prefuse.data.Tuple;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

/**
 * @author Ilya Boyandin
 */
public class FlowMapGraphBuilder {

  private static final String graphNodeIdAttr = FlowMapGraph.GRAPH_NODE_ID_COLUMN;
  private final Graph graph;
  private final FlowMapAttrSpec attrSpec;
  private HashMap<EdgeKey, Edge> cumulatedEdges;
  private final Map<String, Node> nodesById = Maps.newHashMap();

  public FlowMapGraphBuilder(String graphId, FlowMapAttrSpec attrSpec) {
    this.attrSpec = attrSpec;
    graph = new Graph();
    FlowMapGraph.setGraphId(graph, graphId);
    graph.addColumn(graphNodeIdAttr, String.class);
    Table nodeTable = graph.getNodeTable();
    Table edgeTable = graph.getEdgeTable();
    if (attrSpec.hasNodePositions()) {
      nodeTable.addColumn(attrSpec.getNodeLonAttr(), double.class);
      nodeTable.addColumn(attrSpec.getNodeLatAttr(), double.class);
    }
    for (String attr : attrSpec.getFlowWeightAttrs()) {
      edgeTable.addColumn(attr, FlowMapGraph.WEIGHT_COLUMNS_DATA_TYPE);
    }
    nodeTable.addColumn(attrSpec.getNodeLabelAttr(), String.class);
  }

  public FlowMapGraphBuilder addNodeAttr(String name, Class<?> type) {
    graph.getNodeTable().addColumn(name, type);
    return this;
  }

  public FlowMapGraphBuilder addEdgeAttr(String name, Class<?> type) {
    graph.getEdgeTable().addColumn(name, type);
    return this;
  }

  public FlowMapGraphBuilder withCumulatedEdges() {
    this.cumulatedEdges = new HashMap<EdgeKey, Edge>();
    return this;
  }

  public Node addNode(String id, String label) {
    return addNode(id, null, label);
  }

  public Node addNode(Point position, String label) {
    return addNode(null, position, label);
  }

  public Node addNode(String id, Point position, String label) {
    Node node = graph.addNode();
    if (id != null) {
      node.setString(graphNodeIdAttr, id);
    }
    if (attrSpec.hasNodePositions()) {
      if (position == null) {
        throw new IllegalArgumentException("Node positions must be supplied for this flowMapGraph");
      }
      node.setDouble(attrSpec.getNodeLonAttr(), position.x());
      node.setDouble(attrSpec.getNodeLatAttr(), position.y());
    }
    node.set(attrSpec.getNodeLabelAttr(), label);
    nodesById.put(id, node);
    return node;
  }

  public void addNode(Map<String, String> attrValues) {
    Node node = addNode(
      requireValue(attrSpec.getNodeIdAttr(), attrValues),
      new Point(
          parseDouble(requireValue(attrSpec.getNodeLonAttr(), attrValues)),
          parseDouble(requireValue(attrSpec.getNodeLatAttr(), attrValues))),
      requireValue(attrSpec.getNodeLabelAttr(), attrValues)
    );

    setCustomAttrs(node, attrValues, new Predicate<String>() {
      public boolean apply(String attrName) { return !attrSpec.isRequiredNodeAttr(attrName); }
    });
  }

  /**
   * Sets the values of the custom attributes (those which are not specified in attrSpec)
   * of the given tuple (node or edge). If there is no column for an attribute in the table,
   * this method will add one (trying to determine the type of the value first).
   */
  private void setCustomAttrs(Tuple tuple, Map<String, String> attrValues,
      Predicate<String> isCustomAttr) {

    Table table = tuple.getTable();

    for (String attr : Iterables.filter(attrValues.keySet(), isCustomAttr)) {
      String value = attrValues.get(attr);
      if (!isEmptyValue(value)) {
        Class<?> type = determineType(value);
        if (!table.canSet(attr, type)) {
          table.addColumn(attr, type);
        }
        tuple.set(attr, value);
      }
    }
  }

  // TODO: Use type Number (and BigDecimals?) instead of Double
  private Class<?> determineType(String value) {
    try {
      Double.parseDouble(value);
      return double.class;
    } catch (NumberFormatException nfe) {
      // ignore
    }
    return String.class;
  }

  private boolean isEmptyValue(String valueStr) {
    return valueStr == null  ||  valueStr.trim().length() == 0;
  }

  private Iterable<Double> weightAttrValues(List<String> attrs, Map<String, String> attrValues) {
    List<Double> vals = new ArrayList<Double>(attrs.size());
    for (String attr : attrs) {
      vals.add(parseDouble(requireValue(attr, attrValues)));
    }
    return vals;
  }

  private double parseDouble(String str) {
    try {
      if (str.trim().length() == 0) {
        return Double.NaN;
      }
      return Double.parseDouble(str);
    } catch (NumberFormatException nfe) {
      throw new IllegalArgumentException("Cannot parse number '" + str + "'");
    }
  }

  private String requireValue(String attrName, Map<String, String> attrValues) {
    String value = attrValues.get(attrName);
    if (value == null) {
      throw new IllegalArgumentException("No value for column '" + attrName + "'");
    }
    return value;
  }

  public void addEdge(Map<String, String> attrValues) {
    Edge edge = addEdge(
      requireValue(attrSpec.getFlowSrcNodeAttr(), attrValues),
      requireValue(attrSpec.getFlowTargetNodeAttr(), attrValues),
      weightAttrValues(attrSpec.getFlowWeightAttrs(), attrValues)
    );

    setCustomAttrs(edge, attrValues, new Predicate<String>() {
      public boolean apply(String attrName) { return !attrSpec.isRequiredFlowAttr(attrName); }
    });
  }

  public Edge addEdge(String srcId, String targetId, Iterable<Double> weights) {
    return addEdge(nodesById.get(srcId), nodesById.get(targetId), weights);
  }

  public Edge addEdge(Node from, Node to, Iterable<Double> weights) {
    return addEdge(from, to, ArrayUtils.toArrayOfPrimitives(weights));
  }

  public Edge addEdge(Node from, Node to, double ... weights) {
    List<String> weightAttrs = attrSpec.getFlowWeightAttrs();
    if (weights.length != weightAttrs.size()) {
      throw new IllegalArgumentException(
          "Number of supplied weights doesn't match the number of weight attrs");
    }

    EdgeKey key = new EdgeKey(from, to);
    double[] sumWeights = weights.clone();
    Edge edge;
    if (cumulatedEdges == null) {
      edge = graph.addEdge(from, to);
    } else {
      edge = cumulatedEdges.get(key);
      if (edge == null) {
        edge = graph.addEdge(from, to);
        cumulatedEdges.put(key, edge);
      } else {
        for (int i = 0; i < weightAttrs.size(); i++) {
          sumWeights[i] += edge.getDouble(weightAttrs.get(i));
        }
      }
    }

    for (int i = 0; i < weightAttrs.size(); i++) {
      edge.setDouble(weightAttrs.get(i), sumWeights[i]);
    }

    return edge;
  }

  private Graph buildGraph() {
    cumulatedEdges = null;
    return graph;
  }

  public FlowMapGraph build() {
    return new FlowMapGraph(buildGraph(), attrSpec);
  }

  private static class EdgeKey {
    final Node from;
    final Node to;
    public EdgeKey(Node from, Node to) {
      if (from == null  || to == null) {
        throw new IllegalArgumentException();
      }
      this.from = from;
      this.to = to;
    }
    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + System.identityHashCode(from);
      result = prime * result + System.identityHashCode(to);
      return result;
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      EdgeKey other = (EdgeKey) obj;
      if (from != other.from)  // identity check
        return false;
      if (to != other.to)  // identity check
        return false;
      return true;
    }
  }

}
