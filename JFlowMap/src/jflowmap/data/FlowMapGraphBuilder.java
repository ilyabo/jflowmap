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

import jflowmap.FlowMapGraph;
import jflowmap.geom.Point;
import jflowmap.visuals.VisualFlowMapModel;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;

/**
 * @author Ilya Boyandin
 */
public class FlowMapGraphBuilder {

  private final String nodeIdAttr = FlowMapGraph.GRAPH_NODE_TABLE_COLUMN_NAME__ID;
  private String nodeXAttr = VisualFlowMapModel.DEFAULT_NODE_X_ATTR_NAME;
  private String nodeYAttr = VisualFlowMapModel.DEFAULT_NODE_Y_ATTR_NAME;
  private String edgeWeightAttr = VisualFlowMapModel.DEFAULT_EDGE_WEIGHT_ATTR_NAME;
  private String nodeLabelAttr = VisualFlowMapModel.DEFAULT_NODE_LABEL_ATTR_NAME;

  private final Graph graph;
  private HashMap<EdgeKey, Edge> cumulatedEdges;
  private boolean columnsInitialized;

  public FlowMapGraphBuilder(String graphId) {
    graph = new Graph();
    FlowMapGraph.setGraphId(graph, graphId);
  }

  private void initColumns() {
    graph.addColumn(nodeXAttr, double.class);
    graph.addColumn(nodeYAttr, double.class);
    graph.addColumn(edgeWeightAttr, double.class);
    graph.addColumn(nodeLabelAttr, String.class);
    graph.addColumn(nodeIdAttr, String.class);
    columnsInitialized = true;
  }

  public FlowMapGraphBuilder addNodeAttr(String name, Class<?> type) {
    graph.addColumn(name, type);
    return this;
  }

  public FlowMapGraphBuilder withCumulativeEdges() {
    this.cumulatedEdges = new HashMap<EdgeKey, Edge>();
    return this;
  }

  public FlowMapGraphBuilder withNodeXAttr(String attrName) {
    this.nodeXAttr = attrName;
    return this;
  }

  public FlowMapGraphBuilder withNodeYAttr(String attrName) {
    this.nodeYAttr = attrName;
    return this;
  }

  public FlowMapGraphBuilder withEdgeWeightAttr(String attrName) {
    this.edgeWeightAttr = attrName;
    return this;
  }

  public FlowMapGraphBuilder withNodeLabelAttr(String attrName) {
    this.nodeLabelAttr = attrName;
    return this;
  }

  public Node addNode(Point position, String label) {
    return addNode(null, position, label);
  }

  public Node addNode(String id, Point position, String label) {
    if (!columnsInitialized) {
      initColumns();
    }
    Node node = graph.addNode();
    node.setString(nodeIdAttr, id);
    node.setDouble(nodeXAttr, position.x());
    node.setDouble(nodeYAttr, position.y());
    node.set(nodeLabelAttr, label);
    return node;
  }

  public Edge addEdge(Node from, Node to, double weight) {
    if (!columnsInitialized) {
      initColumns();
    }
    EdgeKey key = new EdgeKey(from, to);
    double sumWeight = weight;
    Edge edge;
    if (cumulatedEdges == null) {
      edge = graph.addEdge(from, to);
    } else {
      edge = cumulatedEdges.get(key);
      if (edge == null) {
        edge = graph.addEdge(from, to);
        cumulatedEdges.put(key, edge);
      } else {
        sumWeight += edge.getDouble(edgeWeightAttr);
      }
    }
    edge.setDouble(edgeWeightAttr, sumWeight);
    return edge;
  }

  public Graph build() {
    if (!columnsInitialized) {
      initColumns();
    }
    cumulatedEdges = null;
    return graph;
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
