package jflowmap.data;

import java.util.Collection;
import java.util.List;
import java.util.Map;

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

/**
 * @author Ilya Boyandin
 */
public class FlowMapGraphEdgeAggregator {

  private Map<List<String>, Node> nodesByIds;
  private final FlowMapGraph flowMapGraph;
  private final Function<Edge, Object> groupFunction;
  private Graph aggGraph;

  private FlowMapGraphEdgeAggregator(FlowMapGraph fmg, Function<Edge, Object> groupFunction) {
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
    return new FlowMapGraphEdgeAggregator(fmg, groupFunction).doAggregate();
  }

  private FlowMapGraph doAggregate() {
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
          aggregate(NodeEdgePos.getNodesOfEdges(edges, NodeEdgePos.SOURCE)),
          aggregate(NodeEdgePos.getNodesOfEdges(edges, NodeEdgePos.TARGET)));

      aggregateColumns(edges, newEdge);
    }

    return new FlowMapGraph(aggGraph, flowMapGraph.getAttrSpec());
  }

  private Node aggregate(Iterable<Node> nodes) {
    Iterable<String> nodeIds = nodeIdsOf(nodes);
    Node newNode = nodesByIds.get(nodeIds);
    if (newNode == null) {
      newNode = aggregateColumns(nodes, aggGraph.addNode());
    }
    return newNode;
  }

  private <T extends Tuple> T aggregateColumns(Iterable<T> tuples, T newTuple) {
    for (int i = 0; i < newTuple.getColumnCount(); i++) {
      final int columnIndex = i;
      ValueAggregator agg = getAggregator(newTuple.getColumnType(i));
      Object aggValue = agg.aggregate(Iterables.transform(tuples, new Function<Tuple, Object>() {
        @Override
        public Object apply(Tuple t) {
          return t.get(columnIndex);
        }
      }));
      newTuple.set(i, aggValue);
    }
    return newTuple;
  }

  private ValueAggregator getAggregator(Class<?> columnType) {
    return GraphMLDataTypes.getByType(columnType);
  }

  interface ValueAggregator {
    Object aggregate(Iterable<Object> values);
  }

  private Iterable<String> nodeIdsOf(Iterable<Node> nodes) {
    return Iterables.transform(nodes, new Function<Node, String>() {
      @Override
      public String apply(Node node) {
        return flowMapGraph.getNodeId(node);
      }
    });
  }

}
