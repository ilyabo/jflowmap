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

package jflowmap;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import jflowmap.data.FlowMapGraphBuilder;
import jflowmap.data.FlowMapStats;
import jflowmap.data.MinMax;
import jflowmap.geom.GeomUtils;
import jflowmap.geom.Point;

import org.apache.log4j.Logger;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

/**
 * @author Ilya Boyandin
 */

public class FlowMapGraph {

  private static final String EDGE_WEIGHT_DIFF_COLUMNS_SUFFIX = ":diff";

  private static Logger logger = Logger.getLogger(FlowMapGraph.class);

  public static final String GRAPH_CLIENT_PROPERTY__ID = "id";
  public static final String GRAPH_NODE_TABLE_COLUMN_NAME__ID = "_node_id";

  private static final String SUBDIVISION_POINTS_ATTR_NAME = "_subdivp";

  private final Graph graph;
  private final FlowMapAttrSpec attrSpec;
  private final FlowMapStats stats;

  private final List<String> matchingEdgeWeightAttrNames;

  public FlowMapGraph(Graph graph, FlowMapAttrSpec attrSpec) {
    this(graph, attrSpec, null);
  }

  /**
   * This constructor is intended to be used when the stats have to be
   * induced and not calculated (for instance, in case when a global mapping over
   * a number of flow maps for small multiples must be used).
   * Otherwise, use {@link #FlowMapGraph(Graph, FlowMapAttrSpec)}.
   */
  public FlowMapGraph(Graph graph, FlowMapAttrSpec attrSpec, FlowMapStats stats) {
    attrSpec.checkValidityFor(graph);
    this.graph = graph;
    this.attrSpec = attrSpec;
    this.matchingEdgeWeightAttrNames = ImmutableList.copyOf(
        findEdgeAttrsByWildcard(attrSpec.getEdgeWeightAttrWildcard()));
    if (stats == null) {
      stats = FlowMapStats.createFor(this);
      logger.info("Creating edge weight stats: " + stats.getEdgeWeightStats());
    }
    this.stats = stats;
  }

  public Iterable<Node> nodes() {
    return new Iterable<Node>() {
      @SuppressWarnings("unchecked")
      @Override
      public Iterator<Node> iterator() {
        return getGraph().nodes();
      }
    };
  }

  public Iterable<Edge> edges() {
    return new Iterable<Edge>() {
      @SuppressWarnings("unchecked")
      @Override
      public Iterator<Edge> iterator() {
        return getGraph().edges();
      }
    };
  }

  public String getId() {
    return getGraphId(graph);
  }

  public String getXNodeAttr() {
    return attrSpec.getXNodeAttr();
  }

  public String getYNodeAttr() {
    return attrSpec.getYNodeAttr();
  }

  public String getEdgeWeightAttrWildcard() {
    return attrSpec.getEdgeWeightAttrWildcard();
  }

  public int getEdgeWeightAttrsCount() {
    return matchingEdgeWeightAttrNames.size();
  }

  public List<String> getEdgeWeightAttrNames() {
    return matchingEdgeWeightAttrNames;
  }

  public List<String> getEdgeWeightDiffAttrNames() {
    return ImmutableList.copyOf(Iterables.transform(matchingEdgeWeightAttrNames,
        new Function<String, String>() {
          public String apply(String weightAttr) {
            return getEdgeWeightDiffAttr(weightAttr);
          }
        }));
  }

  public String getNodeLabelAttr() {
    return attrSpec.getNodeLabelAttr();
  }

  public FlowMapStats getStats() {
    return stats;
  }

  public FlowMapAttrSpec getAttrSpec() {
    return attrSpec;
  }

  public Graph getGraph() {
    return graph;
  }

  // TODO: create class FlowMapGraph encapsulating Graph and move these methods there
  public static String getGraphId(Graph graph) {
    return (String) graph.getClientProperty(GRAPH_CLIENT_PROPERTY__ID);
  }

  public static void setGraphId(Graph graph, String name) {
    graph.putClientProperty(GRAPH_CLIENT_PROPERTY__ID, name);
  }

  public String getNodeId(Node node) {
    return getIdOfNode(node);
  }

  private static String getIdOfNode(Node node) {
    return node.getString(GRAPH_NODE_TABLE_COLUMN_NAME__ID);
  }

  public String getNodeLabel(Node node) {
    return node.getString(attrSpec.getNodeLabelAttr());
  }

  public static Node findNodeById(Graph graph, String nodeId) {
    int index = findNodeIndexById(graph, nodeId);
    if (index >= 0) {
      return graph.getNode(index);
    }
    return null;
  }

  public List<String> findEdgeAttrsByWildcard(String wildcard) {
    return findEdgeAttrsByWildcard(graph, wildcard);
  }

  public static List<String> findEdgeAttrsByWildcard(Graph graph, String wildcard) {
    Pattern re = Pattern.compile(wildcard);
    Table et = graph.getEdgeTable();
    List<String> attrs = Lists.newArrayList();
    for (int i = 0; i < et.getColumnCount(); i++) {
      String cname = et.getColumnName(i);
      if (re.matcher(cname).matches()) {
        attrs.add(cname);
      }
    }
    return attrs;
  }

  public static int findNodeIndexById(Graph graph, String nodeId) {
    for (int i = 0, len = graph.getNodeCount(); i < len; i++) {
      Node node = graph.getNode(i);
      if (nodeId.equals(getIdOfNode(node))) {
        return i;
      }
    }
    return -1;
  }

//  public static <T> Set<T> getNodeAttrValues(Graph graph, String attrName) {
//    return getNodeAttrValues(Arrays.asList(graph), attrName);
//  }

  public MinMax getEdgeLengthStats() {
    return stats.getEdgeLengthStats();
  }

  public double getEdgeWeight(Edge edge) {
    return edge.getDouble(attrSpec.getEdgeWeightAttrWildcard());
  }

  public List<Point> getEdgePoints(Edge edge) {
    List<Point> subdiv;
    if (hasEdgeSubdivisionPoints(edge)) {
      subdiv = getEdgeSubdivisionPoints(edge);
    } else {
      subdiv = Collections.emptyList();
    }
    List<Point> points = Lists.newArrayListWithExpectedSize(subdiv.size() + 2);
    points.add(getEdgeSourcePoint(edge));
    points.addAll(subdiv);
    points.add(getEdgeTargetPoint(edge));
    return points;
  }

  public boolean isSelfLoop(Edge edge) {
    Node src = edge.getSourceNode();
    Node target = edge.getTargetNode();
    if (src == target) {
      return true;
    }
    return GeomUtils.isSelfLoopEdge(
        src.getDouble(attrSpec.getXNodeAttr()), target.getDouble(attrSpec.getXNodeAttr()),
        src.getDouble(attrSpec.getYNodeAttr()), target.getDouble(attrSpec.getYNodeAttr())
    );
  }

  public boolean hasEdgeSubdivisionPoints(Edge edge) {
    return
      edge.canGet(SUBDIVISION_POINTS_ATTR_NAME, List.class)  &&
      // the above will return true after calling removeAllEdgeSubdivisionPoints(),
      // so we need to add the following null check:
      (edge.get(SUBDIVISION_POINTS_ATTR_NAME) != null);
  }

  @SuppressWarnings("unchecked")
  public List<Point> getEdgeSubdivisionPoints(Edge edge) {
    checkContainsEdge(edge);
    return (List<Point>) edge.get(SUBDIVISION_POINTS_ATTR_NAME);
  }

  public void setEdgeSubdivisionPoints(Edge edge, List<Point> points) {
    checkContainsEdge(edge);
    if (!graph.hasSet(SUBDIVISION_POINTS_ATTR_NAME)) {
      graph.addColumn(SUBDIVISION_POINTS_ATTR_NAME, List.class);
    }
    edge.set(SUBDIVISION_POINTS_ATTR_NAME, points);
  }

  public void removeAllEdgeSubdivisionPoints() {
    int numEdges = graph.getEdgeCount();
    for (int i = 0; i < numEdges; i++) {
      Edge edge = graph.getEdge(i);
      if (hasEdgeSubdivisionPoints(edge)) {
        edge.set(SUBDIVISION_POINTS_ATTR_NAME, null);
      }
    }
  }

  private void checkContainsEdge(Edge edge) {
    if (!graph.containsTuple(edge)) {
      throw new IllegalArgumentException("Edge is not in graph");
    }
  }

  public Point getEdgeSourcePoint(Edge edge) {
    Node src = edge.getSourceNode();
    return new Point(src.getDouble(attrSpec.getXNodeAttr()),
        src.getDouble(attrSpec.getYNodeAttr()));
  }

  public Point getEdgeTargetPoint(Edge edge) {
    Node target = edge.getTargetNode();
    return new Point(target.getDouble(attrSpec.getXNodeAttr()),
        target.getDouble(attrSpec.getYNodeAttr()));
  }


  @SuppressWarnings("unchecked")
  private <T> Set<T> nodeAttrValues(String attrName) {
    Set<T> values = Sets.newLinkedHashSet();
    for (int i = 0, len = graph.getNodeCount(); i < len; i++) {
      Node node = graph.getNode(i);
      T v = (T) node.get(attrName);
      if (v != null) {
        values.add(v);
      }
    }
    return values;
  }

  /**
   * Creates a new FlowMapGraph in which nodes of this FlowMapGraph
   * having the same value of {@nodeAttrToGroupBy} are grouped together.
   */
  public FlowMapGraph groupNodesBy(String nodeAttrToGroupBy) {
    FlowMapGraphBuilder builder = new FlowMapGraphBuilder(getId())
//          .withCumulativeEdges()      // TODO: why isn't it working?
      .withNodeXAttr(attrSpec.getXNodeAttr())
      .withNodeYAttr(attrSpec.getYNodeAttr())
      .withEdgeWeightAttr(attrSpec.getEdgeWeightAttrWildcard())
      .withNodeLabelAttr(attrSpec.getNodeLabelAttr())
      ;

    Map<Object, Node> valueToNode = Maps.newHashMap();
    for (Object v : nodeAttrValues(nodeAttrToGroupBy)) {
      String strv = v.toString();
      Node node = builder.addNode(strv, new Point(0, 0), strv);
      valueToNode.put(v, node);
    }

    for (int i = 0, numEdges = graph.getEdgeCount(); i < numEdges; i++) {
      Edge e = graph.getEdge(i);
      Node src = e.getSourceNode();
      Node trg = e.getTargetNode();
      String srcV = src.getString(nodeAttrToGroupBy);
      String trgV = trg.getString(nodeAttrToGroupBy);
      if (srcV == null) {
        throw new IllegalArgumentException("No " + nodeAttrToGroupBy + " value for " + src);
      }
      if (trgV == null) {
        throw new IllegalArgumentException("No " + nodeAttrToGroupBy + " value for " + trg);
      }
      builder.addEdge(
          valueToNode.get(srcV),
          valueToNode.get(trgV),
          e.getDouble(attrSpec.getEdgeWeightAttrWildcard()));
    }

    return new FlowMapGraph(builder.build(), attrSpec);
  }

  public Map<String, String> mapOfNodeIdsToAttrValues(String nodeAttr) {
    Map<String, String> nodeIdsToLabels = Maps.newLinkedHashMap();
    for (int i = 0, numNodes = graph.getNodeCount(); i < numNodes; i++) {
      Node node = graph.getNode(i);
      nodeIdsToLabels.put(getNodeId(node), node.getString(nodeAttr));
    }
    return nodeIdsToLabels;
  }

  /**
   * Builds a multimap between the values of the given {@nodeAttr} and the
   * node ids having these values.
   */
  public Multimap<Object, String> multimapOfNodeAttrValuesToNodeIds(String nodeAttr) {
    Multimap<Object, String> multimap = LinkedHashMultimap.create();
    for (int i = 0, numNodes = graph.getNodeCount(); i < numNodes; i++) {
      Node node = graph.getNode(i);
      multimap.put(node.get(nodeAttr), getNodeId(node));
    }
    return multimap;
  }

  public static Function<Graph, FlowMapGraph> funcGraphToFlowMapGraph(
      final FlowMapAttrSpec attrSpec, final FlowMapStats stats) {
    return new Function<Graph, FlowMapGraph>() {
       @Override
      public FlowMapGraph apply(Graph from) {
        return new FlowMapGraph(from, attrSpec, stats);
      }
    };
  }

  /**
   * Loads a flow map graph from the given file and with the given attrSpecs.
   * If there are more than one graphs in the file only the first one will be used.
   */
  public static FlowMapGraph loadGraphML(String filename, FlowMapAttrSpec attrSpec)
    throws IOException
  {
    return loadGraphML(filename, attrSpec, null);
  }

  /**
   * Use when the stats have to be induced and not calculated (e.g. when a global mapping over
   * a number of flow maps for small multiples must be used).
   * Otherwise, use {@link #loadGraphML(String, FlowMapAttrSpec)}.
   */
  public static FlowMapGraph loadGraphML(String filename, FlowMapAttrSpec attrSpec,
      FlowMapStats stats) throws IOException {
    List<FlowMapGraph> list = FlowMapGraphSet.loadGraphMLAsList(filename, attrSpec, stats);
    if (list.isEmpty()) {
      throw new IOException("No graphs found in '" + filename + "'");
    }
    return list.iterator().next();
  }

  public static final Comparator<? super FlowMapGraph> COMPARE_BY_GRAPH_IDS =
    new Comparator<FlowMapGraph>() {
      @Override
      public int compare(FlowMapGraph o1, FlowMapGraph o2) {
        return o1.getId().compareTo(o2.getId());
      }
    };

//  public List<FlowTuple> listFlowTuples(Predicate<Edge> edgeP) {
//    List<FlowTuple> list = Lists.newArrayList();
//    for (int i = 0, numEdges = graph.getEdgeCount(); i < numEdges; i++) {
//      Edge e = graph.getEdge(i);
//      if (edgeP == null  ||  edgeP.apply(e)) {
//        for (String attr : matchingEdgeWeightAttrNames) {
//          list.add(new FlowTuple(srcNodeId, targetNodeId, edges, fmgs));
//        }
//      }
//    }
//  }

  /**
   * Returns max edge weight (for wildcarded weight attrs)
   */
  public double getMaxEdgeWeight(Edge e) {
    double max = Double.NaN;
    for (String attr : getEdgeWeightAttrNames()) {
      double v = e.getDouble(attr);
      if (Double.isNaN(max)  ||  v > max) {
        max = v;
      }
    }
    return max;
  }

  public Comparator<Edge> createMaxWeightComparator() {
    return new Comparator<Edge>() {
      @Override
      public int compare(Edge e1, Edge e2) {
        return (int)Math.signum(getMaxEdgeWeight(e1) - getMaxEdgeWeight(e2));
      }
    };
  }

  public String getEdgeWeightDiffAttr(String weightAttr) {
    return weightAttr + EDGE_WEIGHT_DIFF_COLUMNS_SUFFIX;
  }

  public void addEdgeWeightDifferenceColumns() {
    Iterable<Edge> edges = edges();

    String prevAttr = null;
    for (String attr : getEdgeWeightAttrNames()) {
      String diffAttr = getEdgeWeightDiffAttr(attr);

      graph.getEdges().addColumn(diffAttr, double.class);
      for (Edge edge : edges) {
        double diff;
        if (prevAttr == null) {
          diff = Double.NaN;
        } else {
          diff = edge.getDouble(attr) - edge.getDouble(prevAttr);
        }
        edge.setDouble(diffAttr, diff);
      }

      prevAttr = attr;
    }
  }

  public String getSourceNodeId(Edge edge) {
    return getNodeId(edge.getSourceNode());
  }

  public String getTargetNodeId(Edge edge) {
    return getNodeId(edge.getTargetNode());
  }

}
