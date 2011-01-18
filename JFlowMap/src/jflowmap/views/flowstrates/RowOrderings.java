package jflowmap.views.flowstrates;

import java.util.Collections;
import java.util.Comparator;

import jflowmap.FlowMapGraph;
import jflowmap.NodeEdgePos;
import jflowmap.clustering.Cosine;
import jflowmap.geom.GeomUtils;
import prefuse.data.Edge;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

/**
 * @author Ilya Boyandin
 */
enum RowOrderings {
    MAX_NODE_SUMMARIES("origin totals max") {
      @Override
      public Comparator<Edge> getComparator(FlowMapGraph fmg) {
        return Collections.reverseOrder(fmg.createMaxNodeSummariesForWeightComparator(NodeEdgePos.SOURCE));
      }
    },
    SRC_VPOS("origin vpos") {
      @Override
      public Comparator<Edge> getComparator(final FlowMapGraph fmg) {
        return new Comparator<Edge>() {
          @Override
          public int compare(Edge e1, Edge e2) {
            int c = RowOrderings.compareNodeVPos(fmg, e1, e2, NodeEdgePos.SOURCE);
            if (c == 0) {
              c = compareNodeLabels(fmg, e1, e2, NodeEdgePos.SOURCE);
            }
            if (c == 0) {
              c = RowOrderings.compareNodeVPos(fmg, e1, e2, NodeEdgePos.TARGET);
            }
            return c;
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
            int c = RowOrderings.compareNodeVPos(fmg, e1, e2, NodeEdgePos.TARGET);
            if (c == 0) {
              c = compareNodeLabels(fmg, e1, e2, NodeEdgePos.TARGET);
            }
            if (c == 0) {
              c = RowOrderings.compareNodeVPos(fmg, e1, e2, NodeEdgePos.SOURCE);
            }
            return c;
          }
        };
      }
    },
    MAX_MAGNITUDE_IN_ROW("value max") {
      @Override
      public Comparator<Edge> getComparator(FlowMapGraph fmg) {
        return Collections.reverseOrder(fmg.createMaxEdgeWeightComparator());
      }
    },
    MAX_DIFF_IN_ROW("diff max") {
      @Override
      public Comparator<Edge> getComparator(FlowMapGraph fmg) {
        return Collections.reverseOrder(fmg.createMaxEdgeWeightDiffComparator());
      }
    },
    MAX_DIFF_REL_IN_ROW("relative diff max") {
      @Override
      public Comparator<Edge> getComparator(FlowMapGraph fmg) {
        return Collections.reverseOrder(fmg.createMaxEdgeWeightRelativeDiffComparator());
      }
    },
    AVG_MAGNITUDE_IN_ROW("value avg") {
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
    EUCLIDEAN_DISTANCE("Eucl. dist") {
      @Override
      public Comparator<Edge> getComparator(final FlowMapGraph fmg) {
        return new Comparator<Edge>() {
          Edge sortBy = fmg.getEgdeForSimilaritySorting();
          Iterable<Double> wlist = fmg.getEdgeWeights(sortBy);
          double max = fmg.getStats().getEdgeWeightStats().getMax();

          double distTo(Edge e) {
            if (e == sortBy) {
              return 0;
            } else {
              return GeomUtils.distance(wlist, fmg.getEdgeWeights(e), max);
            }
          }

          @Override
          public int compare(Edge e1, Edge e2) {
            return Double.compare(distTo(e1), distTo(e2));
          }
        };
      }
    },
    COSINE_SIMILARITY("Cosine similarity") {
      @Override
      public Comparator<Edge> getComparator(final FlowMapGraph fmg) {
        return new Comparator<Edge>() {
          Edge sortBy = fmg.getEgdeForSimilaritySorting();
          Iterable<Double> wlist = fmg.getEdgeWeights(sortBy);
          double max = fmg.getStats().getEdgeWeightStats().getMax();

          double distTo(Edge e) {
            if (e == sortBy) {
              return 0;
            } else {
              return Cosine.cosine(wlist, fmg.getEdgeWeights(e));
            }
          }

          @Override
          public int compare(Edge e1, Edge e2) {
            return Double.compare(distTo(e1), distTo(e2));
          }

          Iterable<Double> nansToZeros(Iterable<Double> v) {
            return Iterables.transform(v, new Function<Double, Double>() {
              public Double apply(Double from) {
                if (Double.isNaN(from))
                  return 0.0;
                return from;
              }
            });
          }
        };
      }
    }
    ;


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

    private static int compareNodeVPos(FlowMapGraph fmg, Edge e1, Edge e2, NodeEdgePos s) {
      String yattr = fmg.getAttrSpec().getNodeLatAttr();
      return -(int)Math.signum(s.nodeOf(e1).getDouble(yattr) - s.nodeOf(e2).getDouble(yattr));
    }

    private static int compareNodeLabels(FlowMapGraph fmg, Edge e1, Edge e2, NodeEdgePos s) {
      return fmg.getNodeLabel(s.nodeOf(e1)).compareTo(fmg.getNodeLabel(s.nodeOf(e2)));
    }

  }