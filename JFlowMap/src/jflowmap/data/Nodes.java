package jflowmap.data;

import java.util.Set;

import jflowmap.FlowMapGraph;
import jflowmap.FlowEndpoint;
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

  public static Iterable<Node> unique(Iterable<Node> nodes) {
    Set<Node> unique = Sets.newTreeSet(FlowMapGraph.COMPARE_NODES_BY_IDS);
    for (Node node : nodes) {
      unique.add(node);
    }
    return unique;
  }

  /**
   * @return List of distinct nodes of the specified side for the given list of edges
   */
  public static Iterable<Node> nodesOfEdges(Iterable<Edge> edges, final FlowEndpoint nodePos) {
    return unique(Iterables.transform(edges, new Function<Edge, Node>() {
      public Node apply(Edge e) {
        return nodePos.nodeOf(e);
      }
    }));
  }

  /**
   * @return Unique list of distinct nodes for the given list of edges
   */
  public static Iterable<Node> nodesOfEdges(Iterable<Edge> edges) {
    Set<Node> set = Sets.newLinkedHashSet();
    for (Edge e : edges) {
      set.add(e.getSourceNode());
      set.add(e.getTargetNode());
    }
    return set;
  }

}
