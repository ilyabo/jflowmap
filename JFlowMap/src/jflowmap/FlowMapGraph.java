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

import jflowmap.data.AttrDataTypes;
import jflowmap.data.FlowMapGraphBuilder;
import jflowmap.data.FlowMapStats;
import jflowmap.data.GraphMLReader3;
import jflowmap.data.MinMax;
import jflowmap.geom.GeomUtils;
import jflowmap.geom.Point;
import jflowmap.util.MathUtils;
import jflowmap.util.Tables;

import org.apache.log4j.Logger;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;
import prefuse.data.Tuple;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
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

//  private static final String EDGE_GROUPING_COLUMN = "_GROUPING";

  private static final String EDGE_WEIGHT_DIFF_COLUMNS_SUFFIX = ":diff";
  private static final String EDGE_WEIGHT_REL_DIFF_COLUMNS_SUFFIX = ":rdiff";

  private static Logger logger = Logger.getLogger(FlowMapGraph.class);

  public static final Class<Double> WEIGHT_COLUMNS_DATA_TYPE = double.class;
  public static final String GRAPH_CLIENT_PROPERTY__ID = "id";
  public static final String GRAPH_NODE_TABLE_COLUMN_NAME__ID = "_node_id";

  private static final String SUBDIVISION_POINTS_ATTR_NAME = "_subdivp";

  private final Graph graph;
  private final FlowMapAttrSpec attrSpec;
  private final FlowMapStats stats;

  private final List<String> edgeWeightAttrNames;


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
//    List<String> weightAttrs = Lists.newArrayList(attrSpec.getEdgeWeightAttrs());
//    Collections.sort(weightAttrs);

    List<String> weightAttrs = attrSpec.getEdgeWeightAttrs();
    if (weightAttrs.size() == 0) {
      throw new IllegalArgumentException("FlowMapGraph must have at least one weight attr. " +
      		"Available columns: " + Iterables.toString(Tables.columns(graph.getEdgeTable())));
    }

    this.edgeWeightAttrNames = ImmutableList.copyOf(weightAttrs);
    logger.info("Creating a FlowMapGraph with edge weight attrs: " + edgeWeightAttrNames);
    if (stats == null) {
      stats = FlowMapStats.createFor(this);
      logger.info("Creating edge weight stats: " + stats.getEdgeWeightStats());
    }
    this.stats = stats;
  }

  /**
   * Note: the iterators are not guaranteed to be fail-safe
   */
  public Iterable<Node> nodes() {
    return new Iterable<Node>() {
      @SuppressWarnings("unchecked")
      @Override
      public Iterator<Node> iterator() {
        return getGraph().nodes();
      }
    };
  }

  public Iterable<Node> nodesHavingEdges(final EdgeDirection dir) {
    return Iterables.filter(nodes(), new Predicate<Node>() {
      @Override
      public boolean apply(Node node) {
        return dir.getDegree(node) > 0;
      }
    });
  }

  /**
   * Note: the iterators are not guaranteed to be fail-safe
   */
  public Iterable<Edge> edges() {
    return new Iterable<Edge>() {
      @SuppressWarnings("unchecked")
      @Override
      public Iterator<Edge> iterator() {
        return getGraph().edges();
      }
    };
  }

//  /**
//   * Returns all egdes which constitute the grouping with the given groupingName.
//   * @param groupingName If null, returns edges which are not in any grouping
//   */
//  public Iterable<Edge> edges(final String groupingName) {
//    return Iterables.filter(edges(), new Predicate<Edge>() {
//      @Override
//      public boolean apply(Edge e) {
//        String group = getEdgeGroupingName(e);
//        if (groupingName == null) {
//          return group == null;
//        } else {
//          return groupingName.equals(group);
//        }
//      }
//    });
//  }

  public String getId() {
    return getGraphId(graph);
  }

  public String getXNodeAttr() {
    return attrSpec.getXNodeAttr();
  }

  public String getYNodeAttr() {
    return attrSpec.getYNodeAttr();
  }

  /**
   * @return An immutable list which can thus be reused without defensive copying.
   */
  public List<String> getEdgeWeightAttrs() {
    return attrSpec.getEdgeWeightAttrs();
  }

  public int getEdgeWeightAttrsCount() {
    return edgeWeightAttrNames.size();
  }

  public List<String> getEdgeWeightAttrNames() {
    return edgeWeightAttrNames;
  }

  public List<String> getEdgeWeightDiffAttrNames() {
    return ImmutableList.copyOf(Iterables.transform(edgeWeightAttrNames,
        new Function<String, String>() {
          public String apply(String weightAttr) {
            return getEdgeWeightDiffAttr(weightAttr);
          }
        }));
  }

  public List<String> getEdgeWeightRelativeDiffAttrNames() {
    return ImmutableList.copyOf(Iterables.transform(edgeWeightAttrNames,
        new Function<String, String>() {
          public String apply(String weightAttr) {
            return getEdgeWeightRelativeDiffAttr(weightAttr);
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

  public static String getIdOfNode(Node node) {
    return node.getString(GRAPH_NODE_TABLE_COLUMN_NAME__ID);
  }

  public String getNodeLabel(Node node) {
    return node.getString(attrSpec.getNodeLabelAttr());
  }

  public Node getNodeOf(Edge edge, NodeEdgePos pos) {
    switch (pos) {
    case SOURCE: return edge.getSourceNode();
    case TARGET: return edge.getTargetNode();
    }
    throw new AssertionError();
  }

  public static Node findNodeById(Graph graph, String nodeId) {
    int index = findNodeIndexById(graph, nodeId);
    if (index >= 0) {
      return graph.getNode(index);
    }
    return null;
  }

  public static List<String> findEdgeAttrsByPattern(Graph graph, String pattern) {
    Pattern re = Pattern.compile(pattern);
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

  public double getEdgeWeight(Edge edge, String weightAttr) {
    return edge.getDouble(weightAttr);
  }

  public Iterable<Double> getEdgeWeights(final Edge edge) {
    return Iterables.transform(getEdgeWeightAttrs(), new Function<String, Double>() {
      @Override
      public Double apply(String weightAttr) {
        return getEdgeWeight(edge, weightAttr);
      }
    });
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
   * Creates a new FlowMapGraph in which edges of this FlowMapGraph
   * having the same value of {@nodeAttrToGroupBy} are grouped together.
   */
//  public FlowMapGraph groupEdgesBy(Function<Edge, Object> groupFunction) {

//  .withCumulativeEdges()

//    String strv = v.toString();
//    Node node = builder.addNode(strv, new Point(0, 0), strv);
//    valueToEdge.put(v, node);

    /*
     Requirements:
     - Grouping by src/target nodes
     - Grouping by an src/target node attr
     - References from group nodes/edges to the ones in the group
     - Hierarchy of groupings (is an ordered list enough?)
     - Possibility to obtain edge weight stats for groupings (and merge them with stats of groupings
       on other levels).

     Solution:
     1.Create new FlowMapGraph for each grouping
     or 2. a dedicated EdgeGrouping class


     Heatmap drawing code will have to support 1. or 2.
     And to know which groups are "expanded" and which are not: boolean property per group

     EdgeGrouping could encapsulate the hierarchy and "expanded"

     class EdgeGrouping {
       List<EdgeGroup> getEdgeGroups()
       MinMax getWeightStats()

     }

     */


//    return null;
//  }

  /**
   * Creates a new FlowMapGraph in which nodes of this FlowMapGraph
   * having the same value of {@nodeAttrToGroupBy} are grouped together.
   */
  public FlowMapGraph groupNodesBy(String nodeAttrToGroupBy) {
    FlowMapGraphBuilder builder = new FlowMapGraphBuilder(getId(), attrSpec);
//          .withCumulativeEdges()      // TODO: why isn't it working?

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
          getEdgeWeights(e));
    }

    return builder.build();
  }


//
//  /**
//   * Will add edges summarizing the weight attrs of the existing edges (which were not added by
//   * another grouping) to the graph and mark them as grouping edges with the given
//   * groupingName.
//   */
//  public void groupEdgesInPlace(String groupingName,
//      Function<Edge, Object> groupFunction, GroupingEdgeFactory factory) {
//    // Sort edges by groups
//    Multimap<Object, Edge> mmap = ArrayListMultimap.create();
//    for (Edge e : edges(null)) {
//      mmap.put(groupFunction.apply(e), e);
//    }
//
//    // Create grouped edges
//    for (Object key : mmap.keySet()) {
//      graph.addEdge();
//      graph.getEdges().addTuple(factory.createGroupingEdge(mmap.get(key)));
//    }
//
//  }
//
//  public interface GroupingEdgeFactory {
//    Edge createGroupingEdge(Iterable<Edge> edges);
//  }
//
//  public boolean isGroupingEdge(Edge e) {
//    return (getEdgeGroupingName(e) != null);
//  }
//
//  public String getEdgeGroupingName(Edge e) {
//    if (!e.canGet(EDGE_GROUPING_COLUMN, String.class)) {
//      return null;
//    }
//    return e.getString(EDGE_GROUPING_COLUMN);
//  }
//
//  protected void setNameOfEdgeGrouping(Edge e, String nameOfTheGrouping) {
//    if (e.canGet(EDGE_GROUPING_COLUMN, String.class)) {
//      if (e.getString(EDGE_GROUPING_COLUMN) != null) {
//        throw new IllegalArgumentException("Already a grouping edge");
//      }
//    } else {
//      e.getTable().addColumn(EDGE_GROUPING_COLUMN, String.class);
//    }
//    e.setString(EDGE_GROUPING_COLUMN, nameOfTheGrouping);
//  }




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

  public static FlowMapGraph loadGraphML(DatasetSpec dataset) throws IOException {
    return loadGraphML(dataset, null);
  }

  public static FlowMapGraph loadGraphML(DatasetSpec dataset, FlowMapStats stats) throws IOException {
    Iterator<Graph> it = GraphMLReader3.loadGraphs(dataset.getFilename()).iterator();
    if (!it.hasNext()) {
      throw new IOException("No graphs found in '" + dataset.getFilename() + "'");
    }
    Graph graph = it.next();
    return new FlowMapGraph(graph, dataset.createFlowMapAttrsSpecFor(graph), stats);
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
    return list.get(0);
  }

  /**
   * Returns max node/edge weight over all specified attrs
   */
  public double getMaxAttrValue(Tuple nodeOrEdge, Iterable<String> attrNames) {
    double max = Double.NaN;
    for (String attr : attrNames) {
      double v = nodeOrEdge.getDouble(attr);
      if (Double.isNaN(max)  ||  v > max) {
        max = v;
      }
    }
    return max;
  }

  public double getAvgAttrValue(Tuple nodeOrEdge, Iterable<String> attrNames) {
    double sum = 0;
    int cnt = 0;
    for (String attr : attrNames) {
      double v = nodeOrEdge.getDouble(attr);
      if (!Double.isNaN(v)) {
        sum += v;
        cnt++;
      }
    }
    return (cnt == 0 ? Double.NaN : sum / cnt);
  }

  public Comparator<Node> createMaxNodeAttrValueComparator(final Iterable<String> nodeAttrs) {
    return new Comparator<Node>() {
      @Override
      public int compare(Node n1, Node n2) {
        return MathUtils.compareDoubles_smallestIsNaN(
            getMaxAttrValue(n1, nodeAttrs), getMaxAttrValue(n2, nodeAttrs));
      }
    };
  }

  public Comparator<Edge> createMaxEdgeWeightComparator() {
    return createMaxEdgeWeightComparator(getEdgeWeightAttrNames());
  }

  public Comparator<Edge> createMaxEdgeWeightDiffComparator() {
    return createMaxEdgeWeightComparator(getEdgeWeightDiffAttrNames());
  }

  public Comparator<Edge> createMaxEdgeWeightRelativeDiffComparator() {
    return createMaxEdgeWeightComparator(getEdgeWeightRelativeDiffAttrNames());
  }

  private Comparator<Edge> createMaxEdgeWeightComparator(final List<String> attrs) {
    return new Comparator<Edge>() {
      @Override
      public int compare(Edge e1, Edge e2) {
        return MathUtils.compareDoubles_smallestIsNaN(
            getMaxAttrValue(e1, attrs), getMaxAttrValue(e2, attrs));
      }
    };
  }

  public Comparator<Edge> createAvgEdgeWeightComparator() {
    return new Comparator<Edge>() {
      @Override
      public int compare(Edge e1, Edge e2) {
        return MathUtils.compareDoubles_smallestIsNaN(
            getAvgAttrValue(e1, getEdgeWeightAttrNames()), getAvgAttrValue(e2, getEdgeWeightAttrNames()));
      }
    };
  }

  public String getEdgeWeightDiffAttr(String weightAttr) {
    return weightAttr + EDGE_WEIGHT_DIFF_COLUMNS_SUFFIX;
  }

  public String getEdgeWeightRelativeDiffAttr(String weightAttr) {
    return weightAttr + EDGE_WEIGHT_REL_DIFF_COLUMNS_SUFFIX;
  }

  public void addEdgeWeightDifferenceColumns() {
    Iterable<Edge> edges = edges();

    String prevAttr = null;
    for (String attr : getEdgeWeightAttrNames()) {
      String diffAttr = getEdgeWeightDiffAttr(attr);

      graph.getEdges().addColumn(diffAttr, double.class);

      for (Edge edge : edges) {
        double prevVal = Double.NaN;
        if (prevAttr != null) {
           prevVal = edge.getDouble(prevAttr);
        }
                                             // if there is no prev value, we'll assume it's zero,
                                             // so that we can at least see the absolute value
        double diff = edge.getDouble(attr) - (Double.isNaN(prevVal) ? 0 : prevVal);
        edge.setDouble(diffAttr, diff);
      }

      prevAttr = attr;
    }
  }

  public void addEdgeWeightRelativeDifferenceColumns() {
    Iterable<Edge> edges = edges();

    String prevAttr = null;
    for (String attr : getEdgeWeightAttrNames()) {
      String diffAttr = getEdgeWeightRelativeDiffAttr(attr);

      graph.getEdges().addColumn(diffAttr, double.class);

      for (Edge edge : edges) {
        double rdiff = Double.NaN;
        double prevVal = Double.NaN;
        if (prevAttr != null) {
          prevVal = edge.getDouble(prevAttr);
          if (!Double.isNaN(prevVal)) {
            double val = edge.getDouble(attr);
            if (prevVal == 0) {
              if (val == 0) {
                rdiff = 0;
              } else {
                rdiff = Math.signum(val);
              }
            } else {
              rdiff = (val - prevVal) / prevVal;
              //rdiff = Math.abs(val - prevVal) / ((Math.abs(val) + Math.abs(prevVal))/2);
            }
          }
        }
        edge.setDouble(diffAttr, rdiff);
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


  public static final Comparator<? super FlowMapGraph> COMPARE_BY_GRAPH_IDS =
    new Comparator<FlowMapGraph>() {
      @Override
      public int compare(FlowMapGraph o1, FlowMapGraph o2) {
        return o1.getId().compareTo(o2.getId());
      }
    };

  public static final Comparator<Node> COMPARE_NODES_BY_IDS = new Comparator<Node>() {
    @Override
    public int compare(Node o1, Node o2) {
      return getIdOfNode(o1).compareTo(getIdOfNode(o2));
    }
  };


  private List<String> aggregatableNodeColumns;

  public List<String> getAggregatableNodeColumns() {
    if (aggregatableNodeColumns == null) {
      List<String> columns = Lists.newArrayList();
      columns.add(GRAPH_NODE_TABLE_COLUMN_NAME__ID);
      columns.add(attrSpec.getNodeLabelAttr());
      if (attrSpec.hasNodePositions()) {
        columns.add(attrSpec.getXNodeAttr());
        columns.add(attrSpec.getYNodeAttr());
      }
      aggregatableNodeColumns = ImmutableList.copyOf(columns);
    }
    return aggregatableNodeColumns;
  }

  public List<String> getAggregatableEdgeColumns() {
    return getEdgeWeightAttrNames();
  }

  public boolean hasNonZeroWeight(Edge edge) {
    for (String attr : getEdgeWeightAttrNames()) {
      if (!Double.isNaN(getEdgeWeight(edge, attr))) {
        return true;
      }
    }

    return false;
  }

  public Iterable<Node> sortByAttr(Iterable<Node> nodes, final String attr) {
    List<Node> list = Lists.newArrayList(nodes);
    final AttrDataTypes type = AttrDataTypes.getByType(graph.getNodeTable().getColumnType(attr));
    Collections.sort(list, new Comparator<Node>() {
      @Override
      public int compare(Node n1, Node n2) {
        return type.compare(n1.get(attr), n2.get(attr));
      }
    });

    return list;
  }

}
