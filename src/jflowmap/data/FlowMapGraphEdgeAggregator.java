package jflowmap.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import jflowmap.FlowEndpoint;
import jflowmap.FlowMapGraph;
import jflowmap.data.Graph2.Table2;

import org.apache.log4j.Logger;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;
import prefuse.data.Tuple;

import com.google.common.base.Function;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

/**
 * @author Ilya Boyandin
 */
public class FlowMapGraphEdgeAggregator {

  private static Logger logger = Logger.getLogger(FlowMapGraphEdgeAggregator.class);

  private static final String AGGREGATE_LIST_COLUMN = "_:agg-list";
  private Map<List<String>, Node> nodesByIds;
  private final FlowMapGraph flowMapGraph;
  private final Function<Edge, Object> groupFunction;
  private Graph aggGraph;
  private Map<String, ValueAggregator> customValueAggregators;

  public FlowMapGraphEdgeAggregator(FlowMapGraph fmg, Function<Edge, Object> groupFunction) {
    this.flowMapGraph = fmg;
    this.groupFunction = groupFunction;
  }

  /**
   * When edges are aggregated their nodes are as well aggregated.
   * A -> B  &  A -> C   ==>  A -> (B,C)
   * A -> D  &  E -> F   ==>  (A,E) -> (D.F)
   * If two aggregated edges share the elements of a node, it must be the same node.
   *
   */
  public static FlowMapGraph aggregate(FlowMapGraph fmg, Function<Edge, Object> groupFunction) {
    return new FlowMapGraphEdgeAggregator(fmg, groupFunction).aggregate();
  }

  public enum GroupFunctions implements Function<Edge, Object> {
    MERGE_ALL {
      @Override
      public Object apply(Edge edge) {
        return true;
      }
    },
    SRC_NODE {
      @Override
      public Object apply(Edge edge) {
        return FlowMapGraph.getIdOfNode(edge.getSourceNode());
      }
    },
    TARGET_NODE {
      @Override
      public Object apply(Edge edge) {
        return FlowMapGraph.getIdOfNode(edge.getTargetNode());
      }
    }
    ;
  }

  public FlowMapGraphEdgeAggregator withCustomValueAggregator(
      String attrName, ValueAggregator agg) {
    if (customValueAggregators == null) {
      customValueAggregators = Maps.newHashMap();
    }
    customValueAggregators.put(attrName, agg);
    return this;
  }

  public static boolean isAggregate(Edge edge) {
    return edge.canGet(AGGREGATE_LIST_COLUMN, List.class);
  }

  @SuppressWarnings("unchecked")
  public static List<Edge> getAggregateList(Edge edge) {
    if (!isAggregate(edge)) {
      throw new IllegalArgumentException("Edge does not have aggregate list: " + edge);
    }
    return (List<Edge>) edge.get(AGGREGATE_LIST_COLUMN);
  }

  private static void recursivelyAddBaseAggregates(Edge edge, List<Edge> edges) {
    if (isAggregate(edge)) {
      for (Edge e : getAggregateList(edge)) {
        recursivelyAddBaseAggregates(e, edges);
      }
    } else {
      edges.add(edge);
    }
  }

  public static List<Edge> getBaseAggregateList(Edge edge) {
    List<Edge> list = Lists.newArrayList();
    recursivelyAddBaseAggregates(edge, list);
    return list;
  }


  @SuppressWarnings("unchecked")
  public static List<Node> getAggregateList(Node node) {
    if (!node.canGet(AGGREGATE_LIST_COLUMN, List.class)) {
      throw new IllegalArgumentException("Node does not have aggregate list: " + node);
    }
    return (List<Node>) node.get(AGGREGATE_LIST_COLUMN);
  }

  public FlowMapGraph aggregate() {
    logger.info("Aggregating FlowMapGraph id='" + flowMapGraph.getId() + "', group function: " +
        groupFunction);
    Multimap<Object, Edge> groups = ArrayListMultimap.create();
    for (Edge e : flowMapGraph.edges()) {
      groups.put(groupFunction.apply(e), e);
    }

    nodesByIds = Maps.newHashMap();
    Graph graph = flowMapGraph.getGraph();
    aggGraph = Graph2.create(
        (Table2)graph.getNodeTable().getSchema().instantiate(),
        (Table2)graph.getEdgeTable().getSchema().instantiate());

    addAggListColumn(aggGraph.getNodeTable());
    addAggListColumn(aggGraph.getEdgeTable());

    for (Object group : groups.keySet()) {
      Collection<Edge> edges = groups.get(group);
      Edge newEdge = aggGraph.addEdge(
          aggregateNodes(Nodes.distinctNodesOfEdges(edges, FlowEndpoint.ORIGIN), AggEntity.SOURCE_NODE),
          aggregateNodes(Nodes.distinctNodesOfEdges(edges, FlowEndpoint.DEST), AggEntity.TARGET_NODE));

      aggregateEdges(edges, newEdge);

      newEdge.set(AGGREGATE_LIST_COLUMN, ImmutableList.copyOf(edges));
    }

    FlowMapGraph.setGraphId(aggGraph, flowMapGraph.getId() + " aggregated by " + groupFunction);

    return new FlowMapGraph(aggGraph, flowMapGraph.getAttrSpec());
  }

  private void addAggListColumn(Table et) {
    if (!et.canGet(AGGREGATE_LIST_COLUMN, List.class)) {
      et.addColumn(AGGREGATE_LIST_COLUMN, List.class);
    }
  }

  private void aggregateEdges(Collection<Edge> edges, Edge newEdge) {
    aggregateColumns(edges, newEdge, flowMapGraph.getAggregatableEdgeColumns(), AggEntity.EDGE);
  }

  public enum AggEntity {
    SOURCE_NODE, TARGET_NODE, EDGE;
  }

  private Node aggregateNodes(Iterable<Node> nodes, AggEntity entity) {
//    nodes = Nodes.unique(nodes);
    List<String> nodeIds = nodeIdsOf(nodes);
    Node newNode = nodesByIds.get(nodeIds);  // if a node has degree > 1, we mustn't recreate it
                                             // for each edge
    if (newNode == null) {
      newNode = aggregateColumns(nodes, aggGraph.addNode(),
          flowMapGraph.getAggregatableNodeColumns(), entity);
      nodesByIds.put(nodeIds, newNode);

      newNode.set(AGGREGATE_LIST_COLUMN, ImmutableList.copyOf(nodes));
    }
    return newNode;
  }

  @SuppressWarnings("unchecked")
  private <T extends Tuple> T aggregateColumns(Iterable<T> tuples, T newTuple,
      Iterable<String> columns, AggEntity entity) {
//    if (logger.isDebugEnabled()) {
//      logger.debug("Aggregating columns: " + Iterables.toString(columns));
//    }
    for (final String column : columns) {
      ValueAggregator agg = getAggregator(column, newTuple.getColumnType(column));
      Object aggValue = agg.aggregate(Iterables.transform(tuples, new Function<Tuple, Object>() {
        @Override
        public Object apply(Tuple t) {
          return t.get(column);
        }
      }), (Iterable<Tuple>)tuples, entity);
      newTuple.set(column, aggValue);
    }
    return newTuple;
  }

  private ValueAggregator getAggregator(String columnName, Class<?> columnType) {
    ValueAggregator agg = null;
    if (customValueAggregators != null) {
      agg = customValueAggregators.get(columnName);
    }
    if (agg == null) {
      agg = AttrDataTypes.getByType(columnType);
    }
    return agg;
  }

  public enum ValueAggregators implements ValueAggregator {
    DOUBLE_AVERAGE {
      @Override
      public Object aggregate(Iterable<Object> values, Iterable<Tuple> tuples, AggEntity entity) {
        double sum = 0;
        int count = 0;
        for (Object obj : values) {
          double val = (Double)obj;
          if (!Double.isNaN(val)) {
            sum += val;
            count++;
          }
        }
        if (count == 0) {
          return Double.NaN;
        }
        return sum / count;
      }
    },
    STRING_ONE_OR_NONE {
      @Override
      public Object aggregate(Iterable<Object> values, Iterable<Tuple> tuples, AggEntity entity) {
        if (Iterables.size(values) == 1) {
          return Iterables.get(values, 0);
        }
        return "";
      }
    }
  }

  public interface ValueAggregator {
    Object aggregate(Iterable<Object> values, Iterable<Tuple> tuples, AggEntity entity);
  }

  private List<String> nodeIdsOf(Iterable<Node> nodes) {
    List<String> ids = new ArrayList<String>();
    for (Node node : nodes) {
      ids.add(flowMapGraph.getNodeId(node));
    }
    return ids;
  }

}
