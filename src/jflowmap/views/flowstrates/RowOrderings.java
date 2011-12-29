package jflowmap.views.flowstrates;

import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.Comparator;

import jflowmap.FlowEndpoint;
import jflowmap.FlowMapGraph;
import prefuse.data.Edge;

/**
 * @author Ilya Boyandin
 */
enum RowOrderings implements RowOrdering {
  MAX_NODE_SUMMARIES("origin totals max") {
    @Override
    public Comparator<Edge> getComparator(FlowstratesView fs) {
      return Collections.reverseOrder(fs.getFlowMapGraph().
          createMaxNodeSummariesForWeightComparator(FlowEndpoint.ORIGIN));
    }
  },
  SRC_VPOS("origin vpos") {
    @Override
    public Comparator<Edge> getComparator(final FlowstratesView fs) {
      return new Comparator<Edge>() {
        @Override
        public int compare(Edge e1, Edge e2) {
          int c = RowOrderings.compareNodeVPos(fs, e1, e2, FlowEndpoint.ORIGIN);
          if (c == 0) {
            c = compareNodeLabels(fs.getFlowMapGraph(), e1, e2, FlowEndpoint.ORIGIN);
          }
          if (c == 0) {
            c = RowOrderings.compareNodeVPos(fs, e1, e2, FlowEndpoint.DEST);
          }
          return c;
        }

      };
    }
  },
  TARGET_VPOS("dest vpos") {
    @Override
    public Comparator<Edge> getComparator(final FlowstratesView fs) {
      return new Comparator<Edge>() {
        @Override
        public int compare(Edge e1, Edge e2) {
          int c = RowOrderings.compareNodeVPos(fs, e1, e2, FlowEndpoint.DEST);
          if (c == 0) {
            c = compareNodeLabels(fs.getFlowMapGraph(), e1, e2, FlowEndpoint.DEST);
          }
          if (c == 0) {
            c = RowOrderings.compareNodeVPos(fs, e1, e2, FlowEndpoint.ORIGIN);
          }
          return c;
        }
      };
    }
  },
  MAX_MAGNITUDE_IN_ROW("max value") {
    @Override
    public Comparator<Edge> getComparator(FlowstratesView fs) {
      return Collections.reverseOrder(fs.getFlowMapGraph().createMaxEdgeWeightComparator());
    }
  },
  AVG_MAGNITUDE_IN_ROW("avg value") {
    @Override
    public Comparator<Edge> getComparator(FlowstratesView fs) {
      return Collections.reverseOrder(fs.getFlowMapGraph().createAvgEdgeWeightComparator());
    }
  },
  MAX_DIFF_IN_ROW("max diff") {
    @Override
    public Comparator<Edge> getComparator(FlowstratesView fs) {
      return Collections.reverseOrder(fs.getFlowMapGraph().createMaxEdgeWeightDiffComparator());
    }
  },
  AVG_DIFF_IN_ROW("avg diff") {
    @Override
    public Comparator<Edge> getComparator(FlowstratesView fs) {
      return Collections.reverseOrder(fs.getFlowMapGraph().createAvgEdgeWeightDiffComparator());
    }
  },
  MAX_DIFF_REL_IN_ROW("max relative diff") {
    @Override
    public Comparator<Edge> getComparator(FlowstratesView fs) {
      return Collections.reverseOrder(fs.getFlowMapGraph().createMaxEdgeWeightRelativeDiffComparator());
    }
  },
  AVG_DIFF_REL_IN_ROW("avg relative diff") {
    @Override
    public Comparator<Edge> getComparator(FlowstratesView fs) {
      return Collections.reverseOrder(fs.getFlowMapGraph().createAvgEdgeWeightRelativeDiffComparator());
    }
  },
  /*
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
  */
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

  public abstract Comparator<Edge> getComparator(FlowstratesView fs);

  private static int compareNodeVPos(FlowstratesView fs, Edge e1, Edge e2, FlowEndpoint ep) {
    /*
    String yattr = fs.getFlowMapGraph().getAttrSpec().getNodeLatAttr();
    return -(int)Math.signum(s.nodeOf(e1).getDouble(yattr) - s.nodeOf(e2).getDouble(yattr));
    */
    Point2D c1 = fs.getMapLayer(ep).getCentroidPoint(e1);
    Point2D c2 = fs.getMapLayer(ep).getCentroidPoint(e2);

    double y1 = c1 == null ? Double.NaN : c1.getY();
    double y2 = c2 == null ? Double.NaN : c2.getY();

    return (int)Math.signum(y1 - y2);
  }

  private static int compareNodeLabels(FlowMapGraph fmg, Edge e1, Edge e2, FlowEndpoint s) {
    return fmg.getNodeLabel(s.nodeOf(e1)).compareTo(fmg.getNodeLabel(s.nodeOf(e2)));
  }

}
