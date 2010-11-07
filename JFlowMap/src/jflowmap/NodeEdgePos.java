package jflowmap;


import prefuse.data.Edge;
import prefuse.data.Node;


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
}