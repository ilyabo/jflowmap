package jflowmap.data;

import java.util.Arrays;
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
    this.nodes = Nodes.distinctNodesOfEdges(edges);
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

  public SeqStat getEdgeAttrStats(String key, final Iterable<Tuple> tuples, String edgeAttr) {
    return getEdgeAttrStats(edgeAttr, tuples, Arrays.asList(edgeAttr));
  }

  protected SeqStat getEdgeAttrStats(String key, final Iterable<Tuple> tuples, final List<String> edgeAttrs) {
    return getCachedOrCalc(
        AttrKeys.edgeAttr(key),
        new AttrStatsCalculator() {
          @Override public SeqStat calc() {
            return TupleStats.createFor(tuples, edgeAttrs);
          }
        }
    );
  }

  @Override
  public SeqStat getEdgeWeightAttrStats(String weightAttr) {
    return getEdgeAttrStats(weightAttr,
        edgesAsTuples(),
        Arrays.asList(weightAttr));
  }

  @Override
  public SeqStat getEdgeWeightStats() {
    return getEdgeAttrStats(AttrKeys.EDGE_WEIGHT.name(),
        edgesAsTuples(),
        getAttrSpec().getFlowWeightAttrs());
  }

  @Override
  public SeqStat getEdgeWeightDiffStats() {
    return getEdgeAttrStats(AttrKeys.EDGE_WEIGHT_DIFF.name(),
        edgesAsTuples(),
        getAttrSpec().getFlowWeightDiffAttrs());
  }

  @Override
  public SeqStat getEdgeWeightRelativeDiffStats() {
    return getEdgeAttrStats(AttrKeys.EDGE_WEIGHT_DIFF_REL.name(),
        edgesAsTuples(),
        getAttrSpec().getFlowWeightRelativeDiffAttrs());
  }

  @Override
  public SeqStat getNodeAttrStats(String attrName) {
    return getEdgeAttrStats(AttrKeys.nodeAttr(attrName),
        nodesAsTuples(),
        getAttrSpec().getFlowWeightRelativeDiffAttrs());
  }

  @Override
  public SeqStat getNodeXStats() {
    return getNodeAttrStats(AttrKeys.NODE_X.name());
  }

  @Override
  public SeqStat getNodeYStats() {
    return getNodeAttrStats(AttrKeys.NODE_Y.name());
  }

}
