/*
 * This file is part of JFlowMap.
 *
 * Copyright 2009 Ilya Boyandin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jflowmap.clustering;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jflowmap.geom.GeomUtils;
import jflowmap.views.flowmap.VisualEdge;
import jflowmap.views.flowmap.VisualNode;
import ch.unifr.dmlib.cluster.DistanceMeasure;

import com.google.common.collect.Sets;

/**
 * @author Ilya Boyandin
 */
public enum NodeDistanceMeasure implements DistanceMeasure<VisualNode> {

  COSINE_IN_OUT("Cosine: in/out", NodeFilter.IN_OR_OUT) {
    public double distance(VisualNode t1, VisualNode t2) {
      return Cosine.IN_AND_OUT.distance(t1, t2);
    }
  },
  COSINE_IN("Cosine: in", NodeFilter.IN) {
    public double distance(VisualNode t1, VisualNode t2) {
      return Cosine.IN.distance(t1, t2);
    }
  },
  COSINE_OUT("Cosine: out", NodeFilter.OUT) {
    public double distance(VisualNode t1, VisualNode t2) {
      return Cosine.OUT.distance(t1, t2);
    }
  },
  COSINE2_IN("Cosine2: in", NodeFilter.IN) {
    public double distance(VisualNode t1, VisualNode t2) {
      return Cosine2.IN.distance(t1, t2);
    }
  },
  COSINE2_OUT("Cosine2: out", NodeFilter.OUT) {
    public double distance(VisualNode t1, VisualNode t2) {
      return Cosine2.OUT.distance(t1, t2);
    }
  },
  EUCLIDEAN("Euclidean", NodeFilter.ALL) {
    public double distance(VisualNode t1, VisualNode t2) {
      double dx = t1.getValueX() - t2.getValueX();
      double dy = t1.getValueY() - t2.getValueY();
      double dist = Math.sqrt(dx * dx + dy * dy);
      return dist;
    }
  },
  COSINE_WITH_NODE_PROXIMITY_IN("Cosine wth proximity: incoming", NodeFilter.IN) {
    public double distance(VisualNode t1, VisualNode t2) {
      return CosineWithNodeProximity.IN.distance(t1, t2);
    }
  },
  COSINE_WITH_NODE_PROXIMITY_OUT("Cosine wth proximity: outgoing", NodeFilter.OUT) {
    public double distance(VisualNode t1, VisualNode t2) {
      return CosineWithNodeProximity.OUT.distance(t1, t2);
    }
  },
  COSINE_WITH_NODE_PROXIMITY_IN_OUT("Cosine wth proximity: in/out", NodeFilter.IN_OR_OUT) {
    public double distance(VisualNode t1, VisualNode t2) {
      return CosineWithNodeProximity.IN_AND_OUT.distance(t1, t2);
    }
  },
  COSINE_WITH_IN_OUT_COMBINATION("Cosine wth proximity: in/out combination", NodeFilter.IN_OR_OUT) {
    public double distance(VisualNode t1, VisualNode t2) {
      int numIncoming = t1.getIncomingEdges().size() + t2.getIncomingEdges().size();
      int numOutgoing = t1.getOutgoingEdges().size() + t2.getOutgoingEdges().size();
      double in = CosineWithNodeProximity.IN.distance(t1, t2);
      double out = CosineWithNodeProximity.OUT.distance(t1, t2);
      return (in * numIncoming + out * numOutgoing) / (numIncoming + numOutgoing);
    }
  },
  COSINE_WITH_NODE_PROXIMITY_IN_OUT_EUCLIDEAN("Cosine wth proximity * Euclidean: in and out", NodeFilter.IN_OR_OUT) {
    public double distance(VisualNode t1, VisualNode t2) {
      return CosineWithNodeProximity.IN_AND_OUT.distance(t1, t2) + EUCLIDEAN.distance(t1, t2);
    }
  },
  COMMON_EDGES_IN("Common edges: in", NodeFilter.IN) {
    public double distance(VisualNode t1, VisualNode t2) {
      return CommonEdges.IN.distance(t1, t2);
    }
  },
  COMMON_EDGES_OUT("Common edges: out", NodeFilter.OUT) {
    public double distance(VisualNode t1, VisualNode t2) {
      return CommonEdges.OUT.distance(t1, t2);
    }
  },
  COMMON_EDGES_IN_OUT_AVG("Common edges: in/out avg", NodeFilter.IN_OR_OUT) {
    public double distance(VisualNode t1, VisualNode t2) {
      return (CommonEdges.IN.distance(t1, t2) + CommonEdges.OUT.distance(t1, t2)) / 2;
    }
  },
  COMMON_EDGES_IN_OUT_COMB("Common edges: in/out combination", NodeFilter.IN_OR_OUT) {
    public double distance(VisualNode t1, VisualNode t2) {
      int numIncoming = t1.getIncomingEdges().size() + t2.getIncomingEdges().size();
      int numOutgoing = t1.getOutgoingEdges().size() + t2.getOutgoingEdges().size();
      return (CommonEdges.IN.distance(t1, t2) * numIncoming + CommonEdges.OUT.distance(t1, t2) * numOutgoing)
      / (numIncoming + numOutgoing);
    }
  },
//  COMMON_EDGES_WEIGHTED_IN("Common edges weighted: incoming", NodeFilter.IN) {
//    public double distance(VisualNode t1, VisualNode t2) {
//      return CommonEdges.IN_PRECISE.distance(t1, t2);
//    }
//  },
//  COMMON_EDGES_WEIGHTED_OUT("Common edges weighted: outgoing", NodeFilter.OUT) {
//    public double distance(VisualNode t1, VisualNode t2) {
//      return CommonEdges.OUT_PRECISE.distance(t1, t2);
//    }
//  },
  INCOMING_AND_OUTGOING_EDGES_WITH_WEIGHTS("Edge barycenter: in/out with weights", NodeFilter.IN_OR_OUT) {
    public double distance(VisualNode n1, VisualNode n2) {
      return (EdgeCombinations.IN_WITH_WEIGHTS.distance(n1, n2) + EdgeCombinations.OUT_WITH_WEIGHTS.distance(n1, n2)) / 2;
    }
  },
  INCOMING_EDGES_WITH_WEIGHTS("Edge barycenter: in with weights", NodeFilter.IN) {
    public double distance(VisualNode n1, VisualNode n2) {
      return EdgeCombinations.IN_WITH_WEIGHTS.distance(n1, n2);
    }
  },
  OUTGOING_EDGES_WITH_WEIGHTS("Edge barycenter: out with weights", NodeFilter.OUT) {
    public double distance(VisualNode n1, VisualNode n2) {
      return EdgeCombinations.OUT_WITH_WEIGHTS.distance(n1, n2);
    }
  },
  INCOMING_AND_OUTGOING_EDGES("Edge barycenter: in/out", NodeFilter.IN_OR_OUT) {
    public double distance(VisualNode n1, VisualNode n2) {
      return (EdgeCombinations.IN.distance(n1, n2) + EdgeCombinations.OUT.distance(n1, n2)) / 2;
    }
  },
  INCOMING_EDGES("Edge barycenter: in", NodeFilter.IN) {
    public double distance(VisualNode n1, VisualNode n2) {
      return EdgeCombinations.IN.distance(n1, n2);
    }
  },
  OUTGOING_EDGES("Edge barycenter: out", NodeFilter.OUT) {
    public double distance(VisualNode n1, VisualNode n2) {
      return EdgeCombinations.OUT.distance(n1, n2);
    }
  }
  ;

  public List<VisualNode> filterNodes(List<VisualNode> nodes) {
    return filter.filterNodes(nodes);
  }

  private NodeDistanceMeasure(String name, NodeFilter filter) {
    this.name = name;
    this.filter = filter;
  }

  private String name;
  private NodeFilter filter;

  @Override
  public String toString() {
    return name;
  }

  private enum CommonEdges implements DistanceMeasure<VisualNode> {
    IN(true),
    OUT(false)
    ;

    private boolean incomingNotOutgoing;

    private CommonEdges(boolean incomingNotOutgoing) {
      this.incomingNotOutgoing = incomingNotOutgoing;
    }

    public double distance(VisualNode node1, VisualNode node2) {
      List<VisualNode> oppositeNodes1 = node1.getOppositeNodes(incomingNotOutgoing);
      List<VisualNode> oppositeNodes2 = node2.getOppositeNodes(incomingNotOutgoing);

      int intersectionSize = 0;
      for (VisualNode node : oppositeNodes1) {
        if (oppositeNodes2.contains(node)) {
          intersectionSize++;
        }
      }
      int unionSize = oppositeNodes1.size() + oppositeNodes2.size() - intersectionSize;

      double similarity = (double)intersectionSize / unionSize;
      if (Double.isNaN(similarity)) {
        similarity = 0.0;
      }
      double dist = 1.0 - similarity;

//      if (dist < .16) System.out.println(
//          node1.getLabel() + " - " + node2.getLabel() + ": common = " + intersectionSize + " of " +
//          unionSize + ", dist = " + dist);

      return dist;
    }
  }


  private enum Cosine2 implements DistanceMeasure<VisualNode> {
    IN(true),
    OUT(false);

    private boolean incomingNotOutgoing;

    private Cosine2(boolean incoming) {
      this.incomingNotOutgoing = incoming;
    }

    @Override
    public double distance(VisualNode t1, VisualNode t2) {
      Set<VisualNode> union = Sets.newHashSet();
      union.addAll(t1.getEdgeOppositeNodes(incomingNotOutgoing));
      union.addAll(t2.getEdgeOppositeNodes(incomingNotOutgoing));

      double dist = 1.0 - jflowmap.clustering.Cosine.cosine(
          vectorOfEdgeWeigths(t1, union),
          vectorOfEdgeWeigths(t2, union));

      if (Double.isNaN(dist)) {
        dist = 0.0;
      }
      return dist;
    }

    private double[] vectorOfEdgeWeigths(VisualNode node, Set<VisualNode> oppositeNodes) {
      double[] v = new double[oppositeNodes.size()];
      int i = 0;
      for (VisualNode n : oppositeNodes) {
        VisualEdge ve = node.getEdgeByOppositeNode(n, incomingNotOutgoing);
        if (ve != null) {
          v[i] = ve.getEdgeWeight();
        } else {
          v[i] = 0;
        }
        i++;
      }
      return v;
    }
  }

  /**
   * See http://www.miislita.com/information-retrieval-tutorial/cosine-similarity-tutorial.html
   * @author Ilya Boyandin
   */
  private enum Cosine implements DistanceMeasure<VisualNode> {
    IN(true, false),
    OUT(false, true),
    IN_AND_OUT(true, true),
    ;

    private boolean includeIncoming;
    private boolean includeOutgoing;

    private Cosine(boolean incoming, boolean outgoing) {
      this.includeIncoming = incoming;
      this.includeOutgoing = outgoing;
    }

    private double valueSquareSum(VisualNode node, boolean incoming) {
      double sum = 0;
      for (VisualEdge e : node.getEdges(incoming)) {
        double v = e.getEdgeWeight();
        sum += v * v;
      }
      return sum;
    }

    public double distance(VisualNode node1, VisualNode node2) {
      double numerator = 0;
      if (includeIncoming) {
        numerator += valueProductsSum(node1, node2, true);
      }
      if (includeOutgoing) {
        numerator += valueProductsSum(node1, node2, false);
      }

      double denominator = 0;
      double denomSum1 = 0;
      double denomSum2 = 0;
      if (includeIncoming) {
        denomSum1 += valueSquareSum(node1, true);
        denomSum2 += valueSquareSum(node2, true);
      }
      if (includeOutgoing) {
        denomSum1 += valueSquareSum(node1, false);
        denomSum2 += valueSquareSum(node2, false);
      }
      denominator = Math.sqrt(denomSum1) * Math.sqrt(denomSum2);

      double similarity = numerator / denominator;
      return 1.0 - similarity;
    }

    private double valueProductsSum(VisualNode node1, VisualNode node2, boolean incoming) {
      double sum = 0;
      List<VisualEdge> edges1 = node1.getEdges(incoming);
      for (VisualEdge edge1 : edges1) {
        VisualEdge matchingEdge = findMatchingEdge(edge1, node2, incoming);
        if (matchingEdge != null) {
          sum += edge1.getEdgeWeight() * matchingEdge.getEdgeWeight();
        }
      }
      // it's enough to iterate through edges1, because we only take
      // perfect matches (edges going to/from the same node) into the sum
      return sum;
    }

    /**
     * Finds an incoming or outgoing (depending on the incoming parameter) edge of
     * node2 which matches the given edge1. Meaning that the returned edge goes from/to
     * the same node as edge1.
     */
    private VisualEdge findMatchingEdge(VisualEdge edge1, VisualNode node2, boolean incoming) {
      VisualEdge matchingEdge = null;
      VisualNode opposite1 = edge1.getNode(incoming ? true : false);  // source if incoming, target if outgoing
      // find an edge
      for (VisualEdge edge2 : node2.getEdges(incoming)) {
        VisualNode opposite2 = edge2.getOppositeNode(node2);
        if (opposite1 == opposite2) {
          matchingEdge = edge2;
          break;
        }
      }
      return matchingEdge;
    }
  }


  private enum CosineWithNodeProximity implements DistanceMeasure<VisualNode> {
    IN(true, false),
    OUT(false, true),
    IN_AND_OUT(true, true),
    ;

    private boolean includeIncoming;
    private boolean includeOutgoing;

    private CosineWithNodeProximity(boolean incoming, boolean outgoing) {
      this.includeIncoming = incoming;
      this.includeOutgoing = outgoing;
    }

    public double distance(VisualNode node1, VisualNode node2) {
      SimilarityFraction sf = new SimilarityFraction();
      if (includeIncoming) {
        forEdges(sf, node1, node2, true, true);
        forEdges(sf, node2, node1, true, false);  // same for node2 edges except for the perfect matches
      }
      if (includeOutgoing) {
        forEdges(sf, node1, node2, false, true);
        forEdges(sf, node2, node1, false, false);  // same for node2 edges except for the perfect matches
      }

      if (includeIncoming) {
        sf.squareOfDenominator1 += valueSquareSum(node1, true);
        sf.squareOfDenominator2 += valueSquareSum(node2, true);
      }
      if (includeOutgoing) {
        sf.squareOfDenominator1 += valueSquareSum(node1, false);
        sf.squareOfDenominator2 += valueSquareSum(node2, false);
      }

      return (1.0 - sf.similarity());
    }

    private double valueSquareSum(VisualNode node, boolean incoming) {
      double sum = 0;
      for (VisualEdge e : node.getEdges(incoming)) {
        double v = e.getEdgeWeight();
        sum += v * v;
      }
      return sum;
    }

    private static class SimilarityFraction {
      double numerator = 0;
      double squareOfDenominator1 = 0;
      double squareOfDenominator2 = 0;
      double similarity() {
        double sim = numerator / (Math.sqrt(squareOfDenominator1) * Math.sqrt(squareOfDenominator2));
        if (Double.isNaN(sim))  // means that there are no common/similar edges -> minimum similarity
          return 0.0;
        return sim;
      }
    }

    private SimilarityFraction forEdges(SimilarityFraction sf, VisualNode node1, VisualNode node2,
        boolean incoming,
        boolean allowPerfectMatches) {

      for (VisualEdge e1 : node1.getEdges(incoming)) {
        VisualEdge matchingEdge = null;
        final boolean oppositeIsSource = (incoming ? true : false);
        VisualNode opposite1 = e1.getNode(oppositeIsSource);     // source if incoming, target if outgoing

        // find a matching edge
        double minDist = Double.POSITIVE_INFINITY;
        for (VisualEdge e2 : node2.getEdges(incoming)) {
          VisualNode opposite2 = e2.getOppositeNode(node2);
          if (opposite1 == opposite2) {
            minDist = 0;
            matchingEdge = e2;
            break;
          } else {
            double dist = opposite2.distanceTo(opposite1);
            if (dist < minDist) {
              minDist = dist;
              matchingEdge = e2;
            }
          }
        }
        boolean nonPerfectMatch = minDist > 0;
        if (allowPerfectMatches  ||  nonPerfectMatch) {
          if (matchingEdge != null) {
            double mv = matchingValue(e1, matchingEdge, oppositeIsSource);
            double v = e1.getEdgeWeight();
            sf.numerator += v * mv;
            if (nonPerfectMatch) {
              if (allowPerfectMatches) {
                sf.squareOfDenominator1 += v * v;
                sf.squareOfDenominator2 += mv * mv;
              } else {
                sf.squareOfDenominator2 += v * v;
                sf.squareOfDenominator1 += mv * mv;
              }
            }
          }
        }
      }
      return sf;
    }

    private double matchingValue(VisualEdge edge, VisualEdge matchingEdge, boolean oppositeIsSource) {
      VisualNode n1 = oppositeIsSource ? edge.getSourceNode() : edge.getTargetNode();
      VisualNode n2 = oppositeIsSource ? matchingEdge.getSourceNode() : matchingEdge.getTargetNode();
      double l_avg = (edge.getEdgeLength() + matchingEdge.getEdgeLength())/2;
      return matchingEdge.getEdgeWeight() * l_avg / (l_avg + n1.distanceTo(n2));
    }
  }

  private enum EdgeCombinations implements DistanceMeasure<VisualNode> {
    IN(true, false),
    OUT(false, false),
    IN_WITH_WEIGHTS(true, true),
    OUT_WITH_WEIGHTS(false, true);

    private boolean useEdgeWeights;
    private boolean incomingNotOutgoing;

    private EdgeCombinations(boolean incomingNotOutgoing, boolean useEdgeWeights) {
      this.useEdgeWeights = useEdgeWeights;
      this.incomingNotOutgoing = incomingNotOutgoing;
    }

    public double distance(VisualNode n1, VisualNode n2) {
      double numerator = 0;
      double denominator = 0;
//      double denominator = 1.0;
      int count = 0;
      for (VisualEdge e1 : getEdges(n1))
      for (VisualEdge e2 : getEdges(n2)) {
        VisualNode t1 = getOppositeNode(e1);
        VisualNode t2 = getOppositeNode(e2);
        double d = GeomUtils.distance(
            t1.getValueX(), t1.getValueY(), t2.getValueX(), t2.getValueY());
        if (useEdgeWeights) {
          double w = e1.getEdgeWeight() * e2.getEdgeWeight();
          numerator += d * w;
          denominator += w;
        } else {
          numerator += d;
          denominator++;
        }
        count++;
      }
      double distance;
      if (count == 0) {
        distance = Double.POSITIVE_INFINITY;
      } else {
        distance = numerator / denominator;
      }
      return distance;
    }

    private List<VisualEdge> getEdges(VisualNode n) {
      if (incomingNotOutgoing) {
        return n.getIncomingEdges();
      } else {
        return n.getOutgoingEdges();
      }
    }

    private VisualNode getOppositeNode(VisualEdge e) {
      if (incomingNotOutgoing) {
        return e.getSourceNode();
      } else {
        return e.getTargetNode();
      }
    }
  }


  /**
   * Filters a given list of nodes so that only
   * nodes having in or out edges are left in the list.
   */
  private enum NodeFilter {
    ALL {
      @Override
      protected boolean accept(VisualNode node) {
        return true;
      }
    },
    IN {
      @Override
      protected boolean accept(VisualNode node) {
        return node.hasIncomingEdges();
      }
    },
    OUT {
      @Override
      protected boolean accept(VisualNode node) {
        return node.hasOutgoingEdges();
      }
    },
    IN_OR_OUT {
      @Override
      protected boolean accept(VisualNode node) {
        return node.hasEdges();
      }
    };

    protected abstract boolean accept(VisualNode node);

    public List<VisualNode> filterNodes(List<VisualNode> nodes) {
      List<VisualNode> filtered = new ArrayList<VisualNode>();
      for (VisualNode node : nodes) {
        if (accept(node)) {
          filtered.add(node);
        }
      }
      return filtered;
    }
  }
}
