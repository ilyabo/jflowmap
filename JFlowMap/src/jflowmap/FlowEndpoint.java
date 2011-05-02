package jflowmap;


import java.util.List;

import prefuse.data.Edge;
import prefuse.data.Node;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;


/**
 * Endpoints of a flow edge.
 *
 * @author Ilya Boyandin
 */
public enum FlowEndpoint {

  ORIGIN {
    @Override public Node nodeOf(Edge e) { return e.getSourceNode(); }
    @Override public FlowDirection dir() { return FlowDirection.OUTGOING; }
    @Override public FlowEndpoint opposite() { return DEST; }
  },

  DEST {
    @Override public Node nodeOf(Edge e) { return e.getTargetNode(); }
    @Override public FlowDirection dir() { return FlowDirection.INCOMING; }
    @Override public FlowEndpoint opposite() { return ORIGIN; }
  };

  public abstract Node nodeOf(Edge e);

  public abstract FlowEndpoint opposite();

  /**
   * Edge direction towards the node
   */
  public abstract FlowDirection dir();

  /**
   * Filters a list of edges by a given predicate applied to the edges' nodes
   * on the side of this FlowEndpoint.
   */
  public Iterable<Edge> filterByNodePredicate(List<Edge> edges, final Predicate<Node> acceptNodes) {
    return Iterables.filter(edges, new Predicate<Edge>() {
      @Override
      public boolean apply(Edge e) {
        return acceptNodes.apply(nodeOf(e));
      }
    });
  }

}
