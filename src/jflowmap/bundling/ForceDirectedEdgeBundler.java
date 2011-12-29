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

package jflowmap.bundling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import jflowmap.FlowMapGraph;
import jflowmap.geom.GeomUtils;
import jflowmap.geom.Point;
import jflowmap.geom.Vector2D;

import org.apache.log4j.Logger;

import prefuse.data.Edge;
import prefuse.data.Graph;
import at.fhj.utils.misc.ProgressTracker;

import com.google.common.collect.ImmutableList;

/**
 * This is an implementation of the algorithm described in the paper
 * "Force-Directed Edge Bundling for Graph Visualization" by
 * Danny Holten and Jarke J. van Wijk.
 *
 * @author Ilya Boyandin
 */
public class ForceDirectedEdgeBundler {

  private static final double EPS = 1e-7;

  private static Logger logger = Logger.getLogger(ForceDirectedEdgeBundler.class);

  private Point[][] edgePoints;
  private double[] edgeLengths;

  private List<CompatibleEdge>[] compatibleEdgeLists;
  private Point[] edgeStarts;
  private Point[] edgeEnds;
  private double[] edgeValues;
  private double edgeValueMax, edgeValueMin;
  private int numEdges;
  private int cycle;

  private int P;    // number of subdivision points (will increase with every cycle)
  private double Pdouble; // used to keep the double value to keep the stable increase rate
  private double S;   // step size
  private int I;    // number of iteration steps performed during a cycle

  private final FlowMapGraph flowMapGraph;
  private final ForceDirectedBundlerParameters params;

  private ProgressTracker progressTracker;
//  private MinMax nodeXStats;
//  private MinMax nodeYStats;

  public ForceDirectedEdgeBundler(
      FlowMapGraph flowMapGraph,
      ForceDirectedBundlerParameters params) {
    this.flowMapGraph = flowMapGraph;
    this.params = params;
  }

  public ProgressTracker getProgressTracker() {
    return progressTracker;
  }

  public List<Point> getSubdivisionPoints(int edgeIndex) {
    if (isSelfLoop(edgeIndex)) {
      return Collections.emptyList();
    }
    return ImmutableList.copyOf(
//        Lists.transform(Arrays.asList(edgePoints[edgeIndex]), denormalizeFun)
        Arrays.asList(edgePoints[edgeIndex])
    );
  }

  private void addGraphSubdivisionPoints() {
    Graph graph = flowMapGraph.getGraph();
    for (int i = 0; i < numEdges; i++) {
      flowMapGraph.setEdgeSubdivisionPoints(graph.getEdge(i), getSubdivisionPoints(i));
    }
  }

  public void bundle(ProgressTracker pt) {
    logger.info("FDE bundling started with the following parameters: " + params);
    pt.startTask("Initializing", .05);
    init(pt);
    if (!pt.isCancelled()) {
      pt.taskCompleted();

      // iterative refinement scheme
      int numCycles = params.getNumCycles();
      for (int cycle = 0; cycle < numCycles; cycle++) {
        pt.startTask("Bundling cycle " + (cycle + 1) + " of " + numCycles, cycle, .95 / numCycles);
        nextCycle();
        if (pt.isCancelled()) {
          break;
        }
        pt.taskCompleted();
        addGraphSubdivisionPoints();
      }
    }

    if (pt.isCancelled()) {
      logger.info("FDE bundling cancelled");
    } else {
      pt.processFinished();
      logger.info("FDE bundling finished");
    }
  }


//  private Point normalize(Point p) {
//    return new Point(nodeXStats.normalize(p.x()), nodeYStats.normalize(p.y()));
//  }
//
//  private final Function<Point, Point> denormalizeFun = new Function<Point, Point>() {
//    @Override
//    public Point apply(Point p) {
//      return denormalize(p);
//    }
//  };
//
//  private Point denormalize(Point p) {
//    return new Point(nodeXStats.denormalize(p.x()), nodeYStats.denormalize(p.y()));
//  }

  private void init(ProgressTracker progressTracker) {
    this.progressTracker = progressTracker;

    numEdges = flowMapGraph.getGraph().getEdgeCount();
    edgeLengths = new double[numEdges];
    edgeStarts = new Point[numEdges];
    edgeEnds = new Point[numEdges];
    double evMin = Double.POSITIVE_INFINITY, evMax = Double.NEGATIVE_INFINITY;
    if (params.getEdgeValueAffectsAttraction()) {
      edgeValues = new double[numEdges];
    }

//    nodeXStats = flowMapGraph.getStats().getNodeXStats();
//    nodeYStats = flowMapGraph.getStats().getNodeYStats();

    for (int i = 0; i < numEdges; i++) {
      Edge edge = flowMapGraph.getGraph().getEdge(i);
      edgeStarts[i] = /*normalize*/(flowMapGraph.getEdgeSourcePoint(edge));
      edgeEnds[i] = /*normalize*/(flowMapGraph.getEdgeTargetPoint(edge));
      double length = edgeStarts[i].distanceTo(edgeEnds[i]);
      if (Math.abs(length) < EPS) length = 0.0;
      edgeLengths[i] = length;
      if (params.getEdgeValueAffectsAttraction()) {
        double value = flowMapGraph.getEdgeWeight(edge, params.getEdgeWeightAttr());
        edgeValues[i] = value;
        if (value > evMax) {
          evMax = value;
        }
        if (value < evMin) {
          evMin = value;
        }
      }
    }
    if (params.getEdgeValueAffectsAttraction()) {
      edgeValueMax = evMax;
      edgeValueMin = evMin;
    }

    this.I = params.getI();
    this.P = params.getP();
    this.Pdouble = this.P;
    this.S = params.getS();
//    this.S = 1e-6;

    calcEdgeCompatibilityMeasures();

    cycle = 0;
  }

  @SuppressWarnings("unchecked")
  private void calcEdgeCompatibilityMeasures() {
    progressTracker.startSubtask("Allocating memory", .05);
    progressTracker.subtaskCompleted();

    logger.info("Calculating compatibility measures");
    logger.info("Using " + (params.getUseSimpleCompatibilityMeasure() ? "simple" : "standard") + " compatibility measure");
    progressTracker.startSubtask("Precalculating edge compatibility measures", .95);
    progressTracker.setSubtaskIncUnit(100.0 / numEdges);

    compatibleEdgeLists = new List[numEdges];
    for (int i = 0; i < numEdges; i++) {
      compatibleEdgeLists[i] = new ArrayList<CompatibleEdge>();
    }
    int numTotal = 0;
    int numCompatible = 0;
    double Csum = 0;
    for (int i = 0; i < numEdges; i++) {
      for (int j = 0; j < i; j++) {
        if (progressTracker.isCancelled()) {
          compatibleEdgeLists = null;
          return;
        }

        double C = calcEdgeCompatibility(i, j);
        if (Math.abs(C) >= params.getEdgeCompatibilityThreshold()) {
          compatibleEdgeLists[i].add(new CompatibleEdge(j, C));
          compatibleEdgeLists[j].add(new CompatibleEdge(i, C));
          numCompatible++;
        }
        Csum += Math.abs(C);
        numTotal++;
      }
      progressTracker.incSubtaskProgress();
    }
    if (logger.isDebugEnabled()) {
      logger.debug("Average edge compatibility = " + (Csum / numTotal));
      logger.debug("Compatibility ratio = " + Math.round((numCompatible * 100.0 / numTotal) * 100)/100.0 + "%");
    }
    if (progressTracker.isCancelled()) {
      compatibleEdgeLists = null;
    }
  }

  private static class CompatibleEdge {
    public CompatibleEdge(int edgeIdx, double c) {
      this.edgeIdx = edgeIdx;
      C = c;
    }
    final int edgeIdx;
    final double C;
  }

  private double calcEdgeCompatibility(int i, int j) {
    double C;
    if (params.getUseSimpleCompatibilityMeasure()) {
      C = calcSimpleEdgeCompatibility(i, j);
    } else {
      C = calcStandardEdgeCompatibility(i, j);
    }
    assert(C >= 0  &&  C <= 1.0);
    if (params.getBinaryCompatibility()) {
      if (C >= params.getEdgeCompatibilityThreshold()) {
        C = 1.0;
      } else {
        C = 0.0;
      }
    }
    if (params.getUseRepulsionForOppositeEdges()) {
      Vector2D p = Vector2D.valueOf(edgeStarts[i], edgeEnds[i]);
      Vector2D q = Vector2D.valueOf(edgeStarts[j], edgeEnds[j]);
      double cos = p.dot(q) / (p.length() * q.length());
      if (cos < 0) {
        C = -C;
      }
    }
    return C;
  }

  private double calcSimpleEdgeCompatibility(int i, int j) {
    if (isSelfLoop(i)  ||  isSelfLoop(j)) {
      return 0.0;
    }

    double l_avg = (edgeLengths[i] + edgeLengths[j])/2;
    return l_avg / (l_avg +
        edgeStarts[i].distanceTo(edgeStarts[j]) +
        edgeEnds[i].distanceTo(edgeEnds[j]));
  }

  private double calcStandardEdgeCompatibility(int i, int j) {
    if (isSelfLoop(i)  ||  isSelfLoop(j)) {
      return 0.0;
    }

    Vector2D p = Vector2D.valueOf(edgeStarts[i], edgeEnds[i]);
    Vector2D q = Vector2D.valueOf(edgeStarts[j], edgeEnds[j]);
    Point pm = GeomUtils.midpoint(edgeStarts[i], edgeEnds[i]);
    Point qm = GeomUtils.midpoint(edgeStarts[j], edgeEnds[j]);
    double l_avg = (edgeLengths[i] + edgeLengths[j])/2;

    // angle compatibility
    double Ca;
    if (params.getDirectionAffectsCompatibility()) {
      Ca = (p.dot(q) / (p.length() * q.length()) + 1.0) / 2.0;
    } else {
      Ca = Math.abs(p.dot(q) / (p.length() * q.length()));
    }
    if (Math.abs(Ca) < EPS) { Ca = 0.0; }     // this led to errors (when Ca == -1e-12)
    if (Math.abs(Math.abs(Ca) - 1.0) < EPS) { Ca = 1.0; }

    // scale compatibility
    double Cs = 2 / (
        (l_avg / Math.min(edgeLengths[i], edgeLengths[j]))  +
        (Math.max(edgeLengths[i], edgeLengths[j]) / l_avg)
    );

    // position compatibility
    double Cp = l_avg / (l_avg + pm.distanceTo(qm));

    // visibility compatibility
    double Cv;
    if (Ca * Cs * Cp > .9) {
      // this compatibility measure is only applied if the edges are
      // (almost) parallel, equal in length and close together
      Cv = Math.min(
          visibilityCompatibility(edgeStarts[i], edgeEnds[i], edgeStarts[j], edgeEnds[j]),
          visibilityCompatibility(edgeStarts[j], edgeEnds[j], edgeStarts[i], edgeEnds[i])
      );
    } else {
      Cv = 1.0;
    }

    assert(Ca >= 0  &&  Ca <= 1);
    assert(Cs >= 0  &&  Cs <= 1);
    assert(Cp >= 0  &&  Cp <= 1);
    assert(Cv >= 0  &&  Cv <= 1);

    if (params.getBinaryCompatibility()) {
      double threshold = params.getEdgeCompatibilityThreshold();
      Ca = Ca >= threshold ? 1.0 : 0.0;
      Cs = Cs >= threshold ? 1.0 : 0.0;
      Cp = Cp >= threshold ? 1.0 : 0.0;
      Cv = Cv >= threshold ? 1.0 : 0.0;
    }

    return Ca * Cs * Cp * Cv;
  }

  private static double visibilityCompatibility(Point p0, Point p1, Point q0, Point q1) {
    Point i0 = GeomUtils.projectPointToLine(p0, p1, q0);
    Point i1 = GeomUtils.projectPointToLine(p0, p1, q1);
    Point im = GeomUtils.midpoint(i0, i1);
    Point pm = GeomUtils.midpoint(p0, p1);
    return Math.max(
        0,
        1 - 2 * pm.distanceTo(im) / i0.distanceTo(i1)
    );
  }

  private boolean isSelfLoop(int edgeIdx) {
    return edgeLengths[edgeIdx] == 0.0;
  }

  public void nextCycle() {
    logger.info("FDE bundling cycle " + (cycle + 1));

    double Pdouble = this.Pdouble;
    int P = this.P;
    double S = this.S;
    int I = this.I;


    // Set parameters for the next cycle
    if (cycle > 0) {
//      P *= 2;
      Pdouble *= params.getSubdivisionPointsCycleIncreaseRate();
      P = (int)Math.round(Pdouble);
      S *= (1.0 - params.getStepDampingFactor());
      I = (I * 2) / 3;
    }

    if (progressTracker.isCancelled()) {
      return;
    }
    progressTracker.startSubtask("Adding subdivision points", .1);
    addSubdivisionPoints(P);
    progressTracker.subtaskCompleted();

    // Perform simulation steps

    Point[][] tmpEdgePoints = new Point[numEdges][P];

    for (int step = 0; step < I; step++) {
      if (progressTracker.isCancelled()) {
        return;
      }
      progressTracker.startSubtask("Step " + (step + 1) + " of " + I, (1.0 - .1) / I);
      progressTracker.setSubtaskIncUnit(100.0 / numEdges);
      for (int pe = 0; pe < numEdges; pe++) {
        if (progressTracker.isCancelled()) {
          return;
        }
        Point[] p = edgePoints[pe];
        Point[] newP = tmpEdgePoints[pe];
        if (isSelfLoop(pe)) {
          continue;     // ignore self-loops
        }

        final int numOfSegments = P + 1;
        double k_p = params.getK() / (edgeLengths[pe] * numOfSegments);

        List<CompatibleEdge> compatible = compatibleEdgeLists[pe];

        for (int i = 0; i < P; i++) {
          // spring forces
          Point p_i = p[i];
          Point p_prev = (i == 0 ? edgeStarts[pe] : p[i - 1]);
          Point p_next = (i == P - 1 ? edgeEnds[pe] : p[i + 1]);
          double Fsi_x = (p_prev.x() - p_i.x()) + (p_next.x() - p_i.x());
          double Fsi_y = (p_prev.y() - p_i.y()) + (p_next.y() - p_i.y());

          if (Math.abs(k_p) < 1.0) {
            Fsi_x *= k_p;
            Fsi_y *= k_p;
          }

          // attracting electrostatic forces (for each other compatible edge)
          double Fei_x = 0;
          double Fei_y = 0;
          for (int ci = 0, size = compatible.size(); ci < size; ci++) {
            CompatibleEdge ce = compatible.get(ci);
            final int qe = ce.edgeIdx;
            final double C = ce.C;
            Point q_i = edgePoints[qe][i];

            double v_x = q_i.x() - p_i.x();
            double v_y = q_i.y() - p_i.y();
            if (Math.abs(v_x) > EPS  ||  Math.abs(v_y) > EPS) {  // zero vector has no direction
              double d = Math.sqrt(v_x * v_x + v_y * v_y);  // shouldn't be zero
              double m;
              if (params.getUseInverseQuadraticModel()) {
                m = (C / d) / (d * d);
              } else {
                m = (C / d) / d;
              }
              if (C < 0) {  // means that repulsion is enabled
                m *= params.getRepulsionAmount();
              }
              if (params.getEdgeValueAffectsAttraction()) {
                double coeff = 1.0 + Math.max(-1.0, (edgeValues[qe] - edgeValues[pe])/(edgeValueMax + edgeValueMin));
                m *= coeff;
              }
              if (Math.abs(m * S) > 1.0) {  // this condition is to reduce the "hairy" effect:
                              // a point shouldn't be moved farther than to the
                              // point which attracts it
                m = Math.signum(m) / S;
                              // TODO: this force difference shouldn't be neglected
                              // instead it should make it more difficult to move the
                              // point from it's current position: this should reduce
                              // the effect even more
              }
              v_x *= m;
              v_y *= m;
              Fei_x += v_x;
              Fei_y += v_y;
            }
          }

          double Fpi_x = Fsi_x + Fei_x;
          double Fpi_y = Fsi_y + Fei_y;

          Point np = newP[i];
          if (np == null) {
            np = new Point(p[i].x(), p[i].y());
          }
          np = new Point(np.x() + Fpi_x * S, np.y() + Fpi_y * S);
          newP[i] = np;
        }
        progressTracker.incSubtaskProgress();
      }
      copy(tmpEdgePoints, edgePoints);
      progressTracker.subtaskCompleted();
    }

    if (!progressTracker.isCancelled()) {
      // update params only in case of success (i.e. no exception)
      this.P = P;
      this.Pdouble = Pdouble;
      this.S = S;
      this.I = I;

      cycle++;
    }
  }

  private void addSubdivisionPoints(int P) {
    int prevP;
    if (edgePoints == null  ||  edgePoints.length == 0) {
      prevP = 0;
    } else {
      prevP = edgePoints[0].length;
    }

    logger.debug("Adding subdivision points: " + prevP + " -> " + P);

    // bigger array for subdivision points of the next cycle
    Point[][] newEdgePoints = new Point[numEdges][P];

    // Add subdivision points
    for (int i = 0, numEdges = newEdgePoints.length; i < numEdges; i++) {
      if (isSelfLoop(i)) {
        continue;   // ignore self-loops
      }
      Point[] newPoints = newEdgePoints[i];
      if (cycle == 0) {
        assert(P == 1);
        newPoints[0] = GeomUtils.midpoint(edgeStarts[i], edgeEnds[i]);
      } else {
        List<Point> points = new ArrayList<Point>(Arrays.asList(edgePoints[i]));
        points.add(0, edgeStarts[i]);
        points.add(edgeEnds[i]);

//        final int prevP = edgePoints[i].length;

        double polylineLen = 0;
        double[] segmentLen = new double[prevP + 1];
        for (int j = 0; j < prevP + 1; j++) {
          double segLen = points.get(j).distanceTo(points.get(j + 1));
          segmentLen[j] = segLen;
          polylineLen += segLen;
        }

        double L = polylineLen / (P + 1);
        int curSegment = 0;
        double prevSegmentsLen = 0;
        Point p = points.get(0);
        Point nextP = points.get(1);
        for (int j = 0; j < P; j++) {
          while (segmentLen[curSegment] < L * (j + 1) - prevSegmentsLen) {
            prevSegmentsLen += segmentLen[curSegment];
            curSegment++;
            p = points.get(curSegment);
            nextP = points.get(curSegment + 1);
          }
          double d = L * (j + 1) - prevSegmentsLen;
          newPoints[j] = GeomUtils.between(p, nextP, d / segmentLen[curSegment]);
        }

      }
    }
    edgePoints = newEdgePoints;
  }

  private void copy(Point[][] src, Point[][] dest) {
    if (src.length != dest.length) {
      throw new RuntimeException("Src and dest array sizes mismatch");
    }
    for (int i = 0; i < src.length; i++) {
//      System.arraycopy(src[i], 0, dest[i], 0, src[i].length);
      for (int j = 0, len = src[i].length; j < len; j++) {
        Point ps = src[i][j];
        if (ps == null)
          dest[i][j] = null;
        else
          dest[i][j] = new Point(ps.x(), ps.y());
//        Point2D.Double pd = dest[i][j];
//        pd.setLocation(ps.getX(), ps.getY());
      }
    }
  }
}
