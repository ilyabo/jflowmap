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

    @Override
    public RowOrdering getHeatmapRowByMaxOrdering() {
      return RowOrderings.MAX_MAGNITUDE_IN_ROW;
    }

    @Override
    public RowOrdering getHeatmapRowByAvgOrdering() {
      return RowOrderings.AVG_MAGNITUDE_IN_ROW;
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

    @Override
    public RowOrdering getHeatmapRowByMaxOrdering() {
      return RowOrderings.MAX_DIFF_IN_ROW;
    }

    @Override
    public RowOrdering getHeatmapRowByAvgOrdering() {
      return RowOrderings.AVG_DIFF_IN_ROW;
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

    @Override
    public RowOrdering getHeatmapRowByMaxOrdering() {
      return RowOrderings.MAX_DIFF_REL_IN_ROW;
    }

    @Override
    public RowOrdering getHeatmapRowByAvgOrdering() {
      return RowOrderings.AVG_DIFF_REL_IN_ROW;
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

  public abstract RowOrdering getHeatmapRowByMaxOrdering();

  public abstract RowOrdering getHeatmapRowByAvgOrdering();

}