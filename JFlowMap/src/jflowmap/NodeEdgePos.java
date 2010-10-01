package jflowmap;

import java.util.Collection;

import prefuse.data.Edge;
import prefuse.data.Node;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

/**
 * End of the edge on which a node is placed.
 *
 * @author Ilya Boyandin
 */
public enum NodeEdgePos {
  SOURCE {
    @Override public Node nodeOf(Edge e) { return e.getSourceNode(); }
    @Override public EdgeDirection dir() { return EdgeDirection.OUTGOING; }
  },
  TARGET {
    @Override public Node nodeOf(Edge e) { return e.getTargetNode(); }
    @Override public EdgeDirection dir() { return EdgeDirection.INCOMING; }
  };

  public abstract Node nodeOf(Edge e);

  /**
   * Edge direction towards the node
   */
  public abstract EdgeDirection dir();

  public static Iterable<Node> getNodesOfEdges(Collection<Edge> edges, final NodeEdgePos nodePos) {
    return Iterables.transform(edges, new Function<Edge, Node>() {
      public Node apply(Edge e) {
        return nodePos.nodeOf(e);
      }
    });
  }
}