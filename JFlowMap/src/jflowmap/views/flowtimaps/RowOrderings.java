package jflowmap.views.flowtimaps;

import java.util.Collections;
import java.util.Comparator;

import jflowmap.FlowMapGraph;
import prefuse.data.Edge;

/**
 * @author Ilya Boyandin
 */
enum RowOrderings {
    MAX_MAGNITUDE_IN_ROW("max magnitude in row") {
      @Override
      public Comparator<Edge> getComparator(FlowMapGraph fmg) {
        return Collections.reverseOrder(fmg.createMaxEdgeWeightComparator());
      }
    },
    AVG_MAGNITUDE_IN_ROW("avg magnitude in row") {
      @Override
      public Comparator<Edge> getComparator(FlowMapGraph fmg) {
        return Collections.reverseOrder(fmg.createAvgEdgeWeightComparator());
      }
    },
    SRC_TARGET_NAMES("src,target node names") {
      @Override
      public Comparator<Edge> getComparator(final FlowMapGraph fmg) {
        return new Comparator<Edge>() {
          @Override
          public int compare(Edge e1, Edge e2) {
            int c = fmg.getNodeLabel(e1.getSourceNode()).compareTo(fmg.getNodeLabel(e2.getSourceNode()));
            if (c == 0) {
              c = fmg.getNodeLabel(e1.getTargetNode()).compareTo(fmg.getNodeLabel(e2.getTargetNode()));
            }
            return c;
          }
        };
      }
    },
    SRC_NODE_VERTICAL_POS("src,target node vertical pos") {
      @Override
      public Comparator<Edge> getComparator(final FlowMapGraph fmg) {
        return new Comparator<Edge>() {
          @Override
          public int compare(Edge e1, Edge e2) {
            String yattr = fmg.getAttrSpec().getYNodeAttr();
            if (e1.getSourceNode() != e2.getSourceNode()) {
              return -(int)Math.signum(
                  e1.getSourceNode().getDouble(yattr) - e2.getSourceNode().getDouble(yattr));
            } else {
              return -(int)Math.signum(
                  e1.getTargetNode().getDouble(yattr) - e2.getTargetNode().getDouble(yattr));
            }
          }
        };
      }
    },
    TARGET_NODE_VERTICAL_POS("target,src node vertical pos") {
      @Override
      public Comparator<Edge> getComparator(final FlowMapGraph fmg) {
        return new Comparator<Edge>() {
          @Override
          public int compare(Edge e1, Edge e2) {
            String yattr = fmg.getAttrSpec().getYNodeAttr();
            if (e1.getTargetNode() != e2.getTargetNode()) {
              return -(int)Math.signum(
                  e1.getTargetNode().getDouble(yattr) - e2.getTargetNode().getDouble(yattr));
            } else {
              return -(int)Math.signum(
                  e1.getSourceNode().getDouble(yattr) - e2.getSourceNode().getDouble(yattr));
            }
          }
        };
      }
    };
//    EUCLIDEAN_DIST_FROM_MAX("Euclidean distance from max");

    private final String description;

    private RowOrderings(String description) {
      this.description = description;
    }

    @Override
    public String toString() {
      return description;
    }

    public abstract Comparator<Edge> getComparator(FlowMapGraph fmg);

  }