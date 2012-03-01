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

import org.apache.log4j.Logger;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.expression.AndPredicate;
import prefuse.data.expression.CompositePredicate;
import prefuse.data.expression.FunctionTable;
import prefuse.data.expression.NotPredicate;
import prefuse.data.expression.OrPredicate;
import prefuse.data.expression.parser.ExpressionParser;
import prefuse.util.collections.IntIterator;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

/**
 * @author Ilya Boyandin
 */
public class FlowMapGraphBuilder {

  private static Logger logger = Logger.getLogger(FlowMapGraphBuilder.class);

  private static final String ALL_WEIGHT_ATTRS_PLACEHOLDER = "#weightAttr#";
  private static final String graphNodeIdAttr = FlowMapGraph.GRAPH_NODE_ID_COLUMN;
  private final Graph2 graph;
  private final FlowMapAttrSpec attrSpec;
  private HashMap<EdgeKey, Edge> cumulatedEdges;
  private final Map<String, Node> nodesById = Maps.newHashMap();
  private String nodeFilterExpr;
  private String edgeFilterExpr;
  private String edgeWeightAttrExistsFilterExpr;
  private String edgeWeightAttrForAllFilterExpr;

  private String disaggregatedAttrValuesAttr;

  static {
    FunctionTable.addFunction("ISNAN", IsNaNFunction.class);
  }

  public FlowMapGraphBuilder(String graphId, FlowMapAttrSpec attrSpec) {
    this.attrSpec = attrSpec;

    graph = Graph2.create();

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

  public FlowMapGraphBuilder withDisaggregatedEdges(String weightAttrsAttr) {
    this.disaggregatedAttrValuesAttr = weightAttrsAttr;
    return withCumulatedEdges();
  }

  public FlowMapGraphBuilder withEdgeFilter(String expr) {
    edgeFilterExpr = expr;
    return this;
  }

  public FlowMapGraphBuilder withNodeFilter(String expr) {
    nodeFilterExpr = expr;
    return this;
  }

  public FlowMapGraphBuilder withEdgeWeightAttrExistsFilter(String expr) {
    edgeWeightAttrExistsFilterExpr = expr;
    return this;
  }

  public FlowMapGraphBuilder withEdgeWeightAttrForAllFilter(String expr) {
    edgeWeightAttrForAllFilterExpr = expr;
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
    node.setString(graphNodeIdAttr, id);
    if (nodesById.containsKey(id)) {
      throw new IllegalArgumentException("Duplicate node id '" + id + "'");
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
    if (disaggregatedAttrValuesAttr == null) {
      for (String attr : attrs) {
        vals.add(parseDouble(requireValue(attr, attrValues)));
      }
    } else {
      vals.add(parseDouble(requireValue(disaggregatedAttrValuesAttr, attrValues)));
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

    Node from = requireNode(requireValue(attrSpec.getFlowSrcNodeAttr(), attrValues));
    Node to = requireNode(requireValue(attrSpec.getFlowTargetNodeAttr(), attrValues));

    if (disaggregatedAttrValuesAttr == null) {

      Edge edge = addEdge(from, to, weightAttrValues(attrSpec.getFlowWeightAttrs(), attrValues));
      setCustomAttrs(edge, attrValues, new Predicate<String>() {
        public boolean apply(String attrName) {
         //return !attrSpec.isRequiredFlowAttr(attrName);
          return !attrSpec.isFlowWeightAttr(attrName);
        }
      });

    } else {

      EdgeKey key = new EdgeKey(from, to);
      Edge edge = cumulatedEdges.get(key);
      String attr = requireValue(disaggregatedAttrValuesAttr, attrValues);
      int count = 1;
      if (edge == null) {
        edge = graph.addEdge(from, to);
        cumulatedEdges.put(key, edge);
      } else {
        if (edge.canGetDouble(attr)) {
          count = count + (int)Math.round(edge.getDouble(attr));
        }
      }
      edge.setDouble(attr, count);


    }
  }

  public Edge addEdge(String srcId, String targetId, Iterable<Double> weights) {
    return addEdge(requireNode(srcId), requireNode(targetId), weights);
  }

  private Node requireNode(String nodeId) {
    Node node = nodesById.get(nodeId);
    if (node == null) {
      throw new IllegalArgumentException("Node could not be found by id '" + nodeId + "'");
    }
    return node;
  }

  public Edge addEdge(Node from, Node to, Iterable<Double> weights) {
    return addEdge(from, to, ArrayUtils.toArrayOfPrimitives(weights));
  }

  public Edge addEdge(Node from, Node to, double ... weights) {
    if (disaggregatedAttrValuesAttr != null) {
      throw new IllegalStateException(
          "This method does is not supported in disaggregatedAttrValues mode");
    }
    List<String> weightAttrs = attrSpec.getFlowWeightAttrs();

    EdgeKey key = new EdgeKey(from, to);

    if (weights.length != weightAttrs.size()) {
      throw new IllegalArgumentException(
          "Number of supplied weights doesn't match the number of weight attrs");
    }

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
    filterNodes(graph, nodeFilterExpr);
    filterEdges(graph, edgeFilterExpr);
    List<String> attrs = attrSpec.getFlowWeightAttrs();
    filterEdgesWithWeightAttrForAll(graph, edgeWeightAttrForAllFilterExpr, attrs);
    filterEdgesWithWeightAttrExists(graph, edgeWeightAttrExistsFilterExpr, attrs);
    return graph;
  }

  public static void filterEdgesWithWeightAttrForAll(Graph g, String expr, Iterable<String> weightAttrs) {
    filterEdgesWithWeightAttr(g, expr, weightAttrs, new AndPredicate());
  }

  public static void filterEdgesWithWeightAttrExists(Graph g, String expr, Iterable<String> weightAttrs) {
    filterEdgesWithWeightAttr(g, expr, weightAttrs, new OrPredicate());
  }

  private static void filterEdgesWithWeightAttr(Graph g, String expr, Iterable<String> weightAttrs,
      CompositePredicate cp) {
    if (!Strings.isNullOrEmpty(expr)) {
      logger.info("Filter edges with weight attr: " + expr);
      for (String attr : weightAttrs) {
        String p = expr.replaceAll(ALL_WEIGHT_ATTRS_PLACEHOLDER, attr);
        logger.info("Filter edges: " + p);
        cp.add(filterPredicate(p));
      }
      filterEdges(g, cp);
    }
  }

  public static prefuse.data.expression.Predicate filterPredicate(String expr) {
    return (prefuse.data.expression.Predicate) ExpressionParser.parse(expr, true);
  }

  public static void filterNodes(Graph graph, String expr) {
    if (!Strings.isNullOrEmpty(expr)) {
      logger.info("Filter nodes: " + expr);
      filterNodes(graph, filterPredicate(expr));
    }
  }

  public static void filterEdges(Graph graph, String expr) {
    if (!Strings.isNullOrEmpty(expr)) {
      logger.info("Filter edges: " + expr);
      filterEdges(graph, filterPredicate(expr));
    }
  }

  private static void filterNodes(Graph g, prefuse.data.expression.Predicate p) {
    if (p != null) {
      p = new NotPredicate(p); // remove rows which do NOT satisfy the original predicate
      IntIterator it = g.getNodeTable().rows(p);
      int cnt = 0;
      int numNodes = g.getNodeCount();
      while (it.hasNext()) {
        g.removeNode(it.nextInt());
        cnt++;
      }
      logger.info("Nodes removed by filter query: " + cnt + " of " + numNodes + ", left: " + g.getNodeCount());
    }
  }

  private static void filterEdges(Graph g, prefuse.data.expression.Predicate p) {
    if (p != null) {
      p = new NotPredicate(p);
      int numEdges = g.getEdgeCount();
      IntIterator it = g.getEdgeTable().rows(p);
      int cnt = 0;
      while (it.hasNext()) {
        if (g.removeEdge(it.nextInt())) {
          cnt++;
        }
      }

//      int cnt = 0;
//      @SuppressWarnings("unchecked")
//      List<Integer> toRemove = ImmutableList.copyOf(g.getEdgeTable().rows(p));
//      for (int i : toRemove) {
//        if (g.removeEdge(i)) {
//          cnt++;
//        }
//      }

      logger.info("Edges removed by filter query: " + cnt + " of " + numEdges + ", left: " + g.getEdgeCount());
    }
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
