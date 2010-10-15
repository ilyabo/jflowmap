package jflowmap.views.flowtimaps;

import java.util.Collections;
import java.util.Comparator;

import jflowmap.FlowMapGraph;
import jflowmap.NodeEdgePos;
import prefuse.data.Edge;
import prefuse.data.Node;

/**
 * @author Ilya Boyandin
 */
enum RowOrderings {
    MAX_MAGNITUDE_IN_ROW("max in row") {
      @Override
      public Comparator<Edge> getComparator(FlowMapGraph fmg) {
        return Collections.reverseOrder(fmg.createMaxEdgeWeightComparator());
      }
    },
    AVG_MAGNITUDE_IN_ROW("avg in row") {
      @Override
      public Comparator<Edge> getComparator(FlowMapGraph fmg) {
        return Collections.reverseOrder(fmg.createAvgEdgeWeightComparator());
      }
    },
    SRC_TARGET_NAMES("origin name") {
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
    TARGET_SRC_NAMES("dest name") {
      @Override
      public Comparator<Edge> getComparator(final FlowMapGraph fmg) {
        return new Comparator<Edge>() {
          @Override
          public int compare(Edge e1, Edge e2) {
            int c = fmg.getNodeLabel(e1.getTargetNode()).compareTo(fmg.getNodeLabel(e2.getTargetNode()));
            if (c == 0) {
              c = fmg.getNodeLabel(e1.getSourceNode()).compareTo(fmg.getNodeLabel(e2.getSourceNode()));
            }
            return c;
          }
        };
      }
    },
    SRC_VPOS("origin vpos") {
      @Override
      public Comparator<Edge> getComparator(final FlowMapGraph fmg) {
        return new Comparator<Edge>() {
          @Override
          public int compare(Edge e1, Edge e2) {
            Node n1 = NodeEdgePos.SOURCE.nodeOf(e1);
            Node n2 = NodeEdgePos.SOURCE.nodeOf(e2);

            String yattr = fmg.getAttrSpec().getYNodeAttr();

            int c = (int)Math.signum(n1.getDouble(yattr) - n2.getDouble(yattr));
            if (c == 0) {
              c = SRC_TARGET_NAMES.getComparator(fmg).compare(e1, e2);
            }
            if (c == 0) {
              c = TARGET_VPOS.getComparator(fmg).compare(e1, e2);
            }
            return -c;
          }
        };
      }
    },
    TARGET_VPOS("dest vpos") {
      @Override
      public Comparator<Edge> getComparator(final FlowMapGraph fmg) {
        return new Comparator<Edge>() {
          @Override
          public int compare(Edge e1, Edge e2) {
            Node n1 = NodeEdgePos.TARGET.nodeOf(e1);
            Node n2 = NodeEdgePos.TARGET.nodeOf(e2);

            String yattr = fmg.getAttrSpec().getYNodeAttr();

            int c = (int)Math.signum(n1.getDouble(yattr) - n2.getDouble(yattr));
            if (c == 0) {
              c = TARGET_SRC_NAMES.getComparator(fmg).compare(e1, e2);
            }
            if (c == 0) {
              c = SRC_VPOS.getComparator(fmg).compare(e1, e2);
            }
            return -c;
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