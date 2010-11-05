package jflowmap.data;

import java.util.List;

import jflowmap.FlowMapAttrSpec;
import prefuse.data.Edge;

import com.google.common.collect.ImmutableList;

/**
 * @author Ilya Boyandin
 */
public class EdgeListFlowMapStats extends AbstractFlowMapStats {

  private final List<Edge> edges;

  private EdgeListFlowMapStats(Iterable<Edge> edges, FlowMapAttrSpec attrSpec) {
    super(attrSpec);
    this.edges = ImmutableList.copyOf(edges);
  }

  @Override
  protected Iterable<Edge> edges() {
    return edges;
  }

  public static FlowMapStats createFor(Iterable<Edge> edges, FlowMapAttrSpec attrSpec) {
    return new EdgeListFlowMapStats(edges, attrSpec);
  }

  @Override
  public MinMax getEdgeWeightDiffStats() {
    return null;
  }

  @Override
  public MinMax getEdgeWeightRelativeDiffStats() {
    return null;
  }

  @Override
  public MinMax getEdgeWeightStats() {
    return null;
  }

  @Override
  public MinMax getNodeAttrStats(String attr) {
    return null;
  }

  @Override
  public MinMax getNodeXStats() {
    return null;
  }

  @Override
  public MinMax getNodeYStats() {
    return null;
  }

}
