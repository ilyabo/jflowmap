package jflowmap.data;

import java.util.Set;

import jflowmap.FlowEndpoint;
import jflowmap.FlowMapGraph;
import prefuse.data.Edge;
import prefuse.data.Node;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

/**
 * @author Ilya Boyandin
 */
public class Nodes {

  private Nodes() {
  }

  public static Iterable<Node> distinct(Iterable<Node> nodes) {
    Set<Node> unique = Sets.newTreeSet(FlowMapGraph.COMPARE_NODES_BY_IDS);
    for (Node node : nodes) {
      unique.add(node);
    }
    return unique;
  }

  /**
   * @return List of distinct nodes of the specified side for the given list of edges
   */
  public static Iterable<Node> distinctNodesOfEdges(Iterable<Edge> edges, final FlowEndpoint ep) {
    return distinct(nodesOfEdges(edges, ep));
  }


  /**
   * @return List of nodes of the specified side for the given list of edges.
   *         Not necessarily distinct.
   */
  public static Iterable<Node> nodesOfEdges(Iterable<Edge> edges, final FlowEndpoint ep) {
    return Iterables.transform(edges, new Function<Edge, Node>() {
      public Node apply(Edge e) {
        return ep.nodeOf(e);
      }
    });
  }

  public static Iterable<String> nodeIdsOfEdges(Iterable<Edge> edges, final FlowEndpoint ep) {
    return Iterables.transform(edges, new Function<Edge, String>() {
      public String apply(Edge e) {
        return FlowMapGraph.getIdOfNode((ep.nodeOf(e)));
      }
    });
  }

  /**
   * @return List of distinct nodes for the given list of edges
   */
  public static Iterable<Node> distinctNodesOfEdges(Iterable<Edge> edges) {
    Set<Node> set = Sets.newLinkedHashSet();
    for (Edge e : edges) {
      set.add(e.getSourceNode());
      set.add(e.getTargetNode());
    }
    return set;
  }

  public static Iterable<String> nodeIdsOf(Iterable<Node> nodes) {
    return Iterables.transform(nodes, new Function<Node, String>() {
      @Override
      public String apply(Node node) {
        return FlowMapGraph.getIdOfNode(node);
      }
    });
  }

}
