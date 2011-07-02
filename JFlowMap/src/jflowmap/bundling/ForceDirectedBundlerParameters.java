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

import jflowmap.FlowMapGraph;
import jflowmap.data.FlowMapStats;
import jflowmap.data.SeqStat;
import at.fhj.utils.graphics.AxisMarks;

/**
 * @author Ilya Boyandin
 */
public class ForceDirectedBundlerParameters {
  private final FlowMapGraph flowMapGraph;

  private int numCycles;
  private int P;    // initial number of subdivision points
  private double S;   // step size - shouldn't be higher than 1.0
  private int I;     // number of iteration steps performed during a cycle
  private double K; // global spring constant (used to control the amount of edge bundling by
              // determining the stiffness of the edges)
  private double stepDampingFactor;
  private double edgeCompatibilityThreshold;
  private boolean directionAffectsCompatibility;
  private boolean binaryCompatibility;
  private boolean useInverseQuadraticModel;
  private boolean useRepulsionForOppositeEdges; // for compatible edges going into opposite directions
  private boolean useSimpleCompatibilityMeasure;
  private boolean edgeValueAffectsAttraction;
//  private boolean joinCloseSubdivisionPoints;
  private double repulsionAmount;
  private double subdivisionPointsCycleIncreaseRate;
  private final String edgeWeightAttr;
  private boolean updateViewAfterEachStep = true;

  public ForceDirectedBundlerParameters(FlowMapGraph flowMapGraph, String edgeWeightAttr) {
    this.flowMapGraph = flowMapGraph;
    this.edgeWeightAttr = edgeWeightAttr;
    resetToDefaults();
  }

  public String getEdgeWeightAttr() {
    return edgeWeightAttr;
  }

  public void resetToDefaults() {
    numCycles = 10;
    P = 1;
    FlowMapStats stats = flowMapGraph.getStats();
    SeqStat xStats = stats.getNodeXStats();
    SeqStat yStats = stats.getNodeYStats();
//    MinMax lenStats = graphStats.getEdgeLengthStats();
//    S = 0.4;
//    S = AxisMarks.ordAlpha(Math.min(xStats.getMax() - xStats.getMin(), yStats.getMax() - yStats.getMin()) / 1000) * 4;
//    S = AxisMarks.ordAlpha(Math.min(xStats.getMax() - xStats.getMin(), yStats.getMax() - yStats.getMin()) / 100);
      S = 1.0;
//    S = AxisMarks.ordAlpha(lenStats.getAvg()) * 4 * 1e-3;
//    I = 50;
    I = 100;
//    K = 0.1;
    K = AxisMarks.ordAlpha(Math.min(xStats.getMax() - xStats.getMin(), yStats.getMax() - yStats.getMin()) / 1000);
    repulsionAmount = 1.0;
    stepDampingFactor = 0.5;
    edgeCompatibilityThreshold = 0.60;
    directionAffectsCompatibility = true;
    binaryCompatibility = false;
    useInverseQuadraticModel = false;
    useRepulsionForOppositeEdges = false;
    useSimpleCompatibilityMeasure = false;
    edgeValueAffectsAttraction = false;
//    joinCloseSubdivisionPoints = true;
    subdivisionPointsCycleIncreaseRate = 1.3;
  }

  public double getSubdivisionPointsCycleIncreaseRate() {
    return subdivisionPointsCycleIncreaseRate;
  }

  public void setSubdivisionPointsCycleIncreaseRate(
      double subdivisionPointsCycleIncreaseRate) {
    this.subdivisionPointsCycleIncreaseRate = subdivisionPointsCycleIncreaseRate;
  }

  private void ensureDirectionAffectsCompatibilityValueIsValid() {
    if (useSimpleCompatibilityMeasure  ||  useRepulsionForOppositeEdges) {
      directionAffectsCompatibility = false;
    }
  }

//  public boolean getJoinCloseSubdivisionPoints() {
//    return joinCloseSubdivisionPoints;
//  }
//
//  public void setJoinCloseSubdivisionPoints(boolean joinCloseSubdivisionPoints) {
//    this.joinCloseSubdivisionPoints = joinCloseSubdivisionPoints;
//  }

  public int getNumCycles() {
    return numCycles;
  }

  public void setNumCycles(int numCycles) {
    this.numCycles = numCycles;
  }

  public int getP() {
    return P;
  }

  public void setP(int p) {
    P = p;
  }

  public double getS() {
    return S;
  }

  public void setS(double s) {
    S = s;
  }

  public int getI() {
    return I;
  }

  public void setI(int i) {
    I = i;
  }

  public double getK() {
    return K;
  }

  public void setK(double k) {
    K = k;
  }

  public double getEdgeCompatibilityThreshold() {
    return edgeCompatibilityThreshold;
  }

  public void setEdgeCompatibilityThreshold(double edgeCompatibilityThreshold) {
    this.edgeCompatibilityThreshold = edgeCompatibilityThreshold;
  }

  public boolean getDirectionAffectsCompatibility() {
    return directionAffectsCompatibility;
  }

  public void setDirectionAffectsCompatibility(boolean directionAffectsCompatibility) {
    this.directionAffectsCompatibility = directionAffectsCompatibility;
  }

  public boolean getBinaryCompatibility() {
    return binaryCompatibility;
  }

  public void setBinaryCompatibility(boolean binaryCompatibility) {
    this.binaryCompatibility = binaryCompatibility;
  }

  public boolean getUseInverseQuadraticModel() {
    return useInverseQuadraticModel;
  }

  public void setUseInverseQuadraticModel(boolean useInverseQuadraticModel) {
    this.useInverseQuadraticModel = useInverseQuadraticModel;
  }

  public boolean getUseRepulsionForOppositeEdges() {
    return useRepulsionForOppositeEdges;
  }

  public void setUseRepulsionForOppositeEdges(boolean useRepulsionForOppositeEdges) {
    this.useRepulsionForOppositeEdges = useRepulsionForOppositeEdges;
    ensureDirectionAffectsCompatibilityValueIsValid();
  }

  public boolean getUseSimpleCompatibilityMeasure() {
    return useSimpleCompatibilityMeasure;
  }

  public void setUseSimpleCompatibilityMeasure(
      boolean useSimpleCompatibilityMeasure) {
    this.useSimpleCompatibilityMeasure = useSimpleCompatibilityMeasure;
    ensureDirectionAffectsCompatibilityValueIsValid();
  }

  public double getStepDampingFactor() {
    return stepDampingFactor;
  }

  public void setStepDampingFactor(double stepDampingFactor) {
    this.stepDampingFactor = stepDampingFactor;
  }

  public boolean getEdgeValueAffectsAttraction() {
    return edgeValueAffectsAttraction;
  }

  public void setEdgeValueAffectsAttraction(boolean edgeValueAffectsAttraction) {
    this.edgeValueAffectsAttraction = edgeValueAffectsAttraction;
  }

  public double getRepulsionAmount() {
    return repulsionAmount;
  }

  public void setRepulsionAmount(double repulsionAmount) {
    this.repulsionAmount = repulsionAmount;
  }


  public boolean getUpdateViewAfterEachStep() {
    return updateViewAfterEachStep;
  }

  public void setUpdateViewAfterEachStep(boolean updateViewAfterEachStep) {
    this.updateViewAfterEachStep = updateViewAfterEachStep;
  }

  @Override
  public String toString() {
    return "ForceDirectedBundlerParameters [numCycles=" + numCycles + ", P=" + P + ", S=" + S + ", I=" + I
        + ", K=" + K + ", stepDampingFactor=" + stepDampingFactor + ", edgeCompatibilityThreshold="
        + edgeCompatibilityThreshold + ", directionAffectsCompatibility=" + directionAffectsCompatibility
        + ", binaryCompatibility=" + binaryCompatibility + ", useInverseQuadraticModel="
        + useInverseQuadraticModel + ", useRepulsionForOppositeEdges=" + useRepulsionForOppositeEdges
        + ", useSimpleCompatibilityMeasure=" + useSimpleCompatibilityMeasure
        + ", edgeValueAffectsAttraction=" + edgeValueAffectsAttraction + ", repulsionAmount="
        + repulsionAmount + ", subdivisionPointsCycleIncreaseRate=" + subdivisionPointsCycleIncreaseRate
        + ", edgeWeightAttr=" + edgeWeightAttr + ", updateViewAfterEachStep=" + updateViewAfterEachStep + "]";
  }



}
