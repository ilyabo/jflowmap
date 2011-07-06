package jflowmap.views.flowstrates;

import jflowmap.FlowMapAttrSpec;
import jflowmap.data.FlowMapStats;
import jflowmap.data.SeqStat;

/**
 * @author Ilya Boyandin
 */
public enum ValueType {

  VALUE("original value") {
    @Override
    public SeqStat getSeqStat(FlowMapStats stats) {
      return stats.getEdgeWeightStats();
    }

    @Override
    public String getColumnValueAttr(FlowMapAttrSpec attrSpec, String attr) {
      return attr;
    }
  },

  DIFF("difference") {
    @Override
    public SeqStat getSeqStat(FlowMapStats stats) {
      return stats.getEdgeWeightDiffStats();
    }

    @Override
    public String getColumnValueAttr(FlowMapAttrSpec attrSpec, String columnAttr) {
      return attrSpec.getFlowWeightDiffAttr(columnAttr);
    }
  },

  DIFF_REL("relative diff") {
    @Override
    public SeqStat getSeqStat(FlowMapStats stats) {
      return stats.getEdgeWeightRelativeDiffStats();
    }

    @Override
    public String getColumnValueAttr(FlowMapAttrSpec attrSpec, String columnAttr) {
      return attrSpec.getFlowWeightRelativeDiffAttr(columnAttr);
    }
  };

  private String name;

  private ValueType(String name) {
    this.name = name;
  }

  public abstract SeqStat getSeqStat(FlowMapStats stats);

  public abstract String getColumnValueAttr(FlowMapAttrSpec attrSpec, String columnAttr);

  @Override
  public String toString() {
    return name;
  }

}