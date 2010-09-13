package jflowmap;

import java.util.Collection;

import prefuse.data.Edge;
import prefuse.data.Node;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

/**
 * @author Ilya Boyandin
 */
public enum NodeEdgePos {
  SOURCE {
    @Override public Node nodeOf(Edge e) { return e.getSourceNode(); }
  },
  TARGET {
    @Override public Node nodeOf(Edge e) { return e.getTargetNode(); }
  };
  public abstract Node nodeOf(Edge e);

  public static Iterable<Node> getNodesOfEdges(Collection<Edge> edges, final NodeEdgePos nodePos) {
    return Iterables.transform(edges, new Function<Edge, Node>() {
      public Node apply(Edge e) {
        return nodePos.nodeOf(e);
      }
    });
  }
}