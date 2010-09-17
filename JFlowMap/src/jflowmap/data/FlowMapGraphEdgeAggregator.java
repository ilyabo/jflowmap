package jflowmap.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jflowmap.FlowMapGraph;
import jflowmap.NodeEdgePos;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Tuple;

import com.google.common.base.Function;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

/**
 * @author Ilya Boyandin
 */
public class FlowMapGraphEdgeAggregator {

  private Map<List<String>, Node> nodesByIds;
  private final FlowMapGraph flowMapGraph;
  private final Function<Edge, Object> groupFunction;
  private Graph aggGraph;

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

  // TODO: add withCustomNodeAggregator(column, NodeAggregator)

  public FlowMapGraph aggregate() {
    Multimap<Object, Edge> groups = ArrayListMultimap.create();
    for (Edge e : flowMapGraph.edges()) {
      groups.put(groupFunction.apply(e), e);
    }

    nodesByIds = Maps.newHashMap();
    Graph graph = flowMapGraph.getGraph();
    aggGraph = new Graph(
        graph.getNodeTable().getSchema().instantiate(),
        graph.getEdgeTable().getSchema().instantiate(),
        graph.isDirected());

    for (Object group : groups.keySet()) {
      Collection<Edge> edges = groups.get(group);
      Edge newEdge = aggGraph.addEdge(
          aggregateNodes(NodeEdgePos.getNodesOfEdges(edges, NodeEdgePos.SOURCE)),
          aggregateNodes(NodeEdgePos.getNodesOfEdges(edges, NodeEdgePos.TARGET)));

      aggregateEdges(edges, newEdge);
    }

    return new FlowMapGraph(aggGraph, flowMapGraph.getAttrSpec());
  }

  private void aggregateEdges(Collection<Edge> edges, Edge newEdge) {
    aggregateColumns(edges, newEdge, flowMapGraph.getAggregatableEdgeColumns());
  }

  private Node aggregateNodes(Iterable<Node> nodes) {
    nodes = unique(nodes);
    List<String> nodeIds = nodeIdsOf(nodes);
    Node newNode = nodesByIds.get(nodeIds);
    if (newNode == null) {
      newNode = aggregateColumns(nodes, aggGraph.addNode(),
          flowMapGraph.getAggregatableNodeColumns());

      nodesByIds.put(nodeIds, newNode);
    }
    return newNode;
  }

  private Set<Node> unique(Iterable<Node> nodes) {
    Set<Node> unique = Sets.newTreeSet(FlowMapGraph.COMPARE_NODES_BY_IDS);
    for (Node node : nodes) {
      unique.add(node);
    }
    return unique;
  }

  private <T extends Tuple> T aggregateColumns(Iterable<T> tuples, T newTuple,
      Iterable<String> columns) {
    for (final String column : columns) {
      ValueAggregator agg = getAggregator(newTuple.getColumnType(column));
      Object aggValue = agg.aggregate(Iterables.transform(tuples, new Function<Tuple, Object>() {
        @Override
        public Object apply(Tuple t) {
          return t.get(column);
        }
      }));
      newTuple.set(column, aggValue);
    }
    return newTuple;
  }

  private ValueAggregator getAggregator(Class<?> columnType) {
    return GraphMLDataTypes.getByType(columnType);
  }

  interface ValueAggregator {
    Object aggregate(Iterable<Object> values);
  }

  private List<String> nodeIdsOf(Iterable<Node> nodes) {
    List<String> ids = new ArrayList<String>();
    for (Node node : nodes) {
      ids.add(flowMapGraph.getNodeId(node));
    }
    return ids;
  }

}
