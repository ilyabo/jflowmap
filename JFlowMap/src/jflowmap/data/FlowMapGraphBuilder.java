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

import java.util.HashMap;
import java.util.List;

import jflowmap.FlowMapAttrSpec;
import jflowmap.FlowMapGraph;
import jflowmap.geom.Point;
import jflowmap.util.ArrayUtils;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;

/**
 * @author Ilya Boyandin
 */
public class FlowMapGraphBuilder {

  private static final String nodeIdAttr = FlowMapGraph.GRAPH_NODE_TABLE_COLUMN_NAME__ID;
  private final Graph graph;
  private final FlowMapAttrSpec attrSpec;
  private HashMap<EdgeKey, Edge> cumulatedEdges;

  public FlowMapGraphBuilder(String graphId, FlowMapAttrSpec attrSpec) {
    this.attrSpec = attrSpec;
    graph = new Graph();
    FlowMapGraph.setGraphId(graph, graphId);
    graph.addColumn(nodeIdAttr, String.class);
    Table nodeTable = graph.getNodeTable();
    Table edgeTable = graph.getEdgeTable();
    if (attrSpec.hasNodePositions()) {
      nodeTable.addColumn(attrSpec.getXNodeAttr(), double.class);
      nodeTable.addColumn(attrSpec.getYNodeAttr(), double.class);
    }
    for (String attr : attrSpec.getEdgeWeightAttrs()) {
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
      node.setString(nodeIdAttr, id);
    }
    if (attrSpec.hasNodePositions()) {
      if (position == null) {
        throw new IllegalArgumentException("Node positions must be supplied for this flowMapGraph");
      }
      node.setDouble(attrSpec.getXNodeAttr(), position.x());
      node.setDouble(attrSpec.getYNodeAttr(), position.y());
    }
    node.set(attrSpec.getNodeLabelAttr(), label);
    return node;
  }

  public Edge addEdge(Node from, Node to, Iterable<Double> weights) {
    return addEdge(from, to, ArrayUtils.toArrayOfPrimitives(weights));
  }

  public Edge addEdge(Node from, Node to, double ... weights) {
    List<String> weightAttrs = attrSpec.getEdgeWeightAttrs();
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
