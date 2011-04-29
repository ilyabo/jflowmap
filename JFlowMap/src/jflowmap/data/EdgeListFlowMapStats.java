package jflowmap.data;

import java.util.List;

import jflowmap.FlowMapAttrSpec;
import prefuse.data.Edge;
import prefuse.data.Node;
import prefuse.data.Tuple;

import com.google.common.collect.ImmutableList;

/**
 * @author Ilya Boyandin
 */
public class EdgeListFlowMapStats extends AbstractFlowMapStats {

  private final List<Edge> edges;
  private final Iterable<Node> nodes;

  private EdgeListFlowMapStats(Iterable<Edge> edges, FlowMapAttrSpec attrSpec) {
    super(attrSpec);
    this.edges = ImmutableList.copyOf(edges);
    this.nodes = Nodes.nodesOfEdges(edges);
  }

  @Override
  protected Iterable<Edge> edges() {
    return edges;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  protected Iterable<Tuple> edgesAsTuples() {
    return (Iterable)edges;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  protected Iterable<Tuple> nodesAsTuples() {
    return (Iterable)nodes;
  }

  public static FlowMapStats createFor(Iterable<Edge> edges, FlowMapAttrSpec attrSpec) {
    return new EdgeListFlowMapStats(edges, attrSpec);
  }

  protected MinMax getAttrStats(String key, final Iterable<Tuple> tuples, final List<String> edgeAttrs) {
    return getCachedOrCalc(
        key,
        new AttrStatsCalculator() {
          @Override public MinMax calc() {
            return TupleStats.createFor(tuples, edgeAttrs);
          }
        }
    );
  }

  @Override
  public MinMax getEdgeWeightStats() {
    return getAttrStats(AttrKeys.EDGE_WEIGHT.name(),
        edgesAsTuples(),
        getAttrSpec().getFlowWeightAttrs());
  }

  @Override
  public MinMax getEdgeWeightDiffStats() {
    return getAttrStats(AttrKeys.EDGE_WEIGHT_DIFF.name(),
        edgesAsTuples(),
        getAttrSpec().getFlowWeightDiffAttrs());
  }

  @Override
  public MinMax getEdgeWeightRelativeDiffStats() {
    return getAttrStats(AttrKeys.EDGE_WEIGHT_DIFF_REL.name(),
        edgesAsTuples(),
        getAttrSpec().getFlowWeightRelativeDiffAttrs());
  }

  @Override
  public MinMax getNodeAttrStats(String attrName) {
    return getAttrStats(AttrKeys.nodeAttr(attrName),
        nodesAsTuples(),
        getAttrSpec().getFlowWeightRelativeDiffAttrs());
  }

  @Override
  public MinMax getNodeXStats() {
    return getNodeAttrStats(AttrKeys.NODE_X.name());
  }

  @Override
  public MinMax getNodeYStats() {
    return getNodeAttrStats(AttrKeys.NODE_Y.name());
  }

}
