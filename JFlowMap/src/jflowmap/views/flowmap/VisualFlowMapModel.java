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

package jflowmap.views.flowmap;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import jflowmap.FlowMapGraph;
import jflowmap.bundling.ForceDirectedBundlerParameters;
import jflowmap.data.FlowMapStats;
import jflowmap.data.SeqStat;
import jflowmap.data.ViewConfig;
import jflowmap.views.MapBackgroundImage;
import jflowmap.views.flowstrates.ValueType;

import org.apache.log4j.Logger;

/**
 * @author Ilya Boyandin
 */
public class VisualFlowMapModel {

  private static Logger logger = Logger.getLogger(VisualFlowMapModel.class);

  public static final String DEFAULT_NODE_X_ATTR_NAME = "x";
  public static final String DEFAULT_NODE_Y_ATTR_NAME = "y";
  public static final String DEFAULT_EDGE_WEIGHT_ATTR_NAME = "value";
  public static final String DEFAULT_NODE_LABEL_ATTR_NAME = "label";

  public static final String PROPERTY_AUTO_VISUAL_LEGEND_SCALE = "visualLegendScale";
  public static final String PROPERTY_AUTO_ADJUST_COLOR_SCALE = "autoAdjustColorScale";
  public static final String PROPERTY_USE_LOG_COLOR_SCALE = "useLogColorScale";
  public static final String PROPERTY_USE_LOG_WIDTH_SCALE = "useLogWidthScale";
  public static final String PROPERTY_MAX_LENGTH_FILTER = "lengthFilterMax";
  public static final String PROPERTY_MIN_LENGTH_FILTER = "lengthFilterMin";
  public static final String PROPERTY_VALUE_FILTER_MIN = "weightFilterMin";
  public static final String PROPERTY_VALUE_FILTER_MAX = "valueFilterMax";
  public static final String PROPERTY_DIRECTION_MARKER_ALPHA = "directionMarkerAlpha";
  public static final String PROPERTY_DIRECTION_MARKER_SIZE = "directionMarkerSize";
  public static final String PROPERTY_EDGE_LENGTH_FILTER_MIN = "edgeLengthFilterMin";
  public static final String PROPERTY_EDGE_LENGTH_FILTER_MAX = "edgeLengthFilterMax";
  public static final String PROPERTY_AUTO_ADJUST_EDGE_COLOR_SCALE = "autoAdjustEdgeColorScale";
  public static final String PROPERTY_MAX_EDGE_WIDTH = "maxEdgeWidth";
  public static final String PROPERTY_EDGE_ALPHA = "edgeAlpha";
  public static final String PROPERTY_USE_PROPORTIONAL_DIRECTION_MARKERS = "proportionalDirMarkers";
  public static final String PROPERTY_FILL_EDGES_WITH_GRADIENT = "fillEdgesWithGradient";
  public static final String PROPERTY_SHOW_DIRECTION_MARKERS = "showDirectionMarkers";
  public static final String PROPERTY_SHOW_SELF_LOOPS = "showSelfLoops";
  public static final String PROPERTY_SHOW_NODES = "showNodes";
  public static final String PROPERTY_NODE_SIZE = "nodeSize";

  public static final String VIEWCONF_WEIGHT_FILTER_MIN = "view.flowmap.weightFilterMin";
  public static final String VIEWCONF_WEIGHT_FILTER_MAX = "view.flowmap.weightFilterMax";
  public static final String VIEWCONF_LENGTH_FILTER_MIN = "view.flowmap.lengthFilterMin";
  public static final String VIEWCONF_LENGTH_FILTER_MAX = "view.flowmap.lengthFilterMax";
  public static final String VIEWCONF_COLOR_SCHEME = "view.flowmap.colorScheme";
  public static final String VIEWCONF_FILL_EDGES_WITH_GRADIENT = "view.flowmap.fillEdgesWithGradient";
  public static final String VIEWCONF_SHOW_DIRECTION_MARKERS = "view.flowmap.showDirectionMarkers";
  public static final String VIEWCONF_SHOW_NODES = "view.flowmap.showNodes";
  public static final String VIEWCONF_EDGE_WIDTH = "view.flowmap.edgeWidth";
  public static final String VIEWCONF_EDGE_OPACITY = "view.flowmap.edgeOpacity";
  public static final String VIEWCONF_DIRECTION_MARKERS_OPACITY = "view.flowmap.directionMarkersOpacity";
  public static final String VIEWCONF_DIRECTION_MARKERS_SIZE = "view.flowmap.directionMarkersSize";

  public static final String VIEWCONF_SHOW_SELF_LOOPS = "view.flowmap.showSelfLoops";
  public static final String VIEWCONF_VALUE_TYPE = "view.flowmap.valueType";
  public static final String VIEWCONF_SHOW_FLOW_WEIGHT_ATTR_LABEL = "view.flowmap.showFlowWeightAttrLabel";

  public static final String VIEWCONF_BUNDLING_UPDATE_VIEW_AFTER_EACH_STEP =
    "view.flowmap.edgeBundling.updateViewAfterEachStep";
  public static final String VIEWCONF_BUNDLING_NUM_OF_CYCLES =
    "view.flowmap.edgeBundling.numberOfCycles";
  public static final String VIEWCONF_BUNDLING_EDGE_STIFFNESS =
    "view.flowmap.edgeBundling.edgeStiffness";
  public static final String VIEWCONF_BUNDLING_COMPATIBILITY_THRESHOLD =
     "view.flowmap.edgeBundling.compatibilityThreshold";
  public static final String VIEWCONF_BUNDLING_STEP_SIZE =
    "view.flowmap.edgeBundling.stepSize";
  public static final String VIEWCONF_BUNDLING_STEP_DAMPING_FACTOR =
    "view.flowmap.edgeBundling.stepDampingFactor";
  public static final String VIEWCONF_BUNDLING_STEPS_IN_1ST_CYCLE =
    "view.flowmap.edgeBundling.stepsIn1stCycle";
  public static final String VIEWCONF_BUNDLING_SIMPLE_COMPATIBILITY_MEASURE =
    "view.flowmap.edgeBundling.simpleCompatibilityMeasure";

  private boolean autoAdjustColorScale;
  private boolean useLogColorScale = true;
  private boolean useLogWidthScale = false;
  private boolean showNodes = true;
  private boolean showDirectionMarkers = true;
  private boolean showSelfLoops = true;
  private boolean fillEdgesWithGradient = true;
  private boolean useProportionalDirectionMarkers = true;


//  private int edgeAlpha = 150;
  private int edgeAlpha = 100;  //50;
//  private int directionMarkerAlpha = 200;
  private int directionMarkerAlpha = 245; //210;

  private double edgeWeightFilterMin;
  private double edgeWeightFilterMax;

  private double edgeLengthFilterMin;
  private double edgeLengthFilterMax;

  private boolean autoAdjustEdgeColorScale;
  private double maxEdgeWidth = 1.0;
  private double directionMarkerSize = .17; //0.1;
  private double nodeSize = 4;
  private double visualLegendScale = 1.0;

  private ValueType valueType = ValueType.VALUE;

  private final PropertyChangeSupport changes = new PropertyChangeSupport(this);
  private final FlowMapGraph flowMapGraph;
  private final MapBackgroundImage mapBackgroundImage;
  private final ViewConfig config;


  public VisualFlowMapModel(FlowMapGraph flowMapGraph, ViewConfig config) {
    this.flowMapGraph = flowMapGraph;
    this.config = config;
    this.mapBackgroundImage = MapBackgroundImage.parseConfig(config);

    flowMapGraph.addEdgeWeightDifferenceColumns();
    flowMapGraph.addEdgeWeightRelativeDifferenceColumns();

    initFromStats();
  }

  public static VisualFlowMapModel createFor(FlowMapGraph fmg, ViewConfig config) {
    VisualFlowMapModel model = new VisualFlowMapModel(fmg, config);
    model.setDirectionMarkerAlpha(config.getIntOrElse(VIEWCONF_DIRECTION_MARKERS_OPACITY, model.getDirectionMarkerAlpha()));
    model.setDirectionMarkerSize(config.getDoubleOrElse(VIEWCONF_DIRECTION_MARKERS_SIZE, model.getDirectionMarkerSize()));
    model.setEdgeAlpha(config.getIntOrElse(VIEWCONF_EDGE_OPACITY, model.getEdgeAlpha()));
    model.setMaxEdgeWidth(config.getDoubleOrElse(VIEWCONF_EDGE_WIDTH, model.getMaxEdgeWidth()));

    double minWeight = config.getDoubleOrElse(VIEWCONF_WEIGHT_FILTER_MIN, Double.NaN);
    if (!Double.isNaN(minWeight)) {
      model.setEdgeWeightFilterMin(minWeight);
    }
    double maxWeight = config.getDoubleOrElse(VIEWCONF_WEIGHT_FILTER_MAX, Double.NaN);
    if (!Double.isNaN(maxWeight)) {
      model.setEdgeWeightFilterMax(maxWeight);
    }

    double minLength = config.getDoubleOrElse(VIEWCONF_LENGTH_FILTER_MIN, Double.NaN);
    if (!Double.isNaN(minLength)) {
      model.setEdgeLengthFilterMin(minLength);
    }
    double maxLength = config.getDoubleOrElse(VIEWCONF_LENGTH_FILTER_MAX, Double.NaN);
    if (!Double.isNaN(maxLength)) {
      model.setEdgeLengthFilterMax(maxLength);
    }
    model.setShowDirectionMarkers(config.getBoolOrElse(VIEWCONF_SHOW_DIRECTION_MARKERS, true));
    model.setShowSelfLoops(config.getBoolOrElse(VIEWCONF_SHOW_SELF_LOOPS, true));
    model.setShowNodes(config.getBoolOrElse(VIEWCONF_SHOW_NODES, true));
    model.setFillEdgesWithGradient(config.getBoolOrElse(VIEWCONF_FILL_EDGES_WITH_GRADIENT, true));

    model.setValueType(ValueType.valueOf(
        config.getStringOrElse(VIEWCONF_VALUE_TYPE, "value").toUpperCase()));

    return model;
  }

  public void setValueType(ValueType valueType) {
    if (this.valueType != valueType) {
      logger.info("Setting value type to: " + valueType);
      this.valueType = valueType;
      // TODO: update
    }
  }

  public ValueType getValueType() {
    return valueType;
  }

  public SeqStat getValueStat() {
    return valueType.getSeqStat(getFlowMapGraph().getStats());
  }

  private void initFromStats() {
    FlowMapStats stat = flowMapGraph.getStats();

    SeqStat lenStat = stat.getEdgeLengthStats();
    this.edgeLengthFilterMin = lenStat.getMin();
    this.edgeLengthFilterMax = lenStat.getMax();

    SeqStat valueStat = valueType.getSeqStat(stat);
    this.edgeWeightFilterMin = valueStat.getMin();
    this.edgeWeightFilterMax = valueStat.getMax();

    this.maxEdgeWidth = Math.min(Math.floor(valueStat.getMax() - valueStat.getMin()), 10.0);
  }

  public void setVisualLegendScale(double scale) {
    double old = this.visualLegendScale;
    this.visualLegendScale = scale;
    changes.firePropertyChange(PROPERTY_AUTO_VISUAL_LEGEND_SCALE, old, scale);
  }

  public double getVisualLegendScale() {
    return visualLegendScale;
  }

  public FlowMapGraph getFlowMapGraph() {
    return flowMapGraph;
  }

  public boolean getAutoAdjustColorScale() {
    return autoAdjustColorScale;
  }

  public void setAutoAdjustColorScale(final boolean autoAdjustColorScale) {
    boolean old = this.autoAdjustColorScale;
    this.autoAdjustColorScale = autoAdjustColorScale;
    changes.firePropertyChange(PROPERTY_AUTO_ADJUST_COLOR_SCALE, old, autoAdjustColorScale);
  }

  public boolean getUseLogColorScale() {
    return useLogColorScale;
  }

  public void setUseLogColorScale(final boolean useLogColorScale) {
    boolean old = this.useLogColorScale;
    this.useLogColorScale = useLogColorScale;
    changes.firePropertyChange(PROPERTY_USE_LOG_COLOR_SCALE, old, useLogColorScale);
  }

  public boolean getUseLogWidthScale() {
    return useLogWidthScale;
  }

  public void setUseLogWidthScale(final boolean useLogWidthScale) {
    boolean old = this.useLogWidthScale;
    this.useLogWidthScale = useLogWidthScale;
    changes.firePropertyChange(PROPERTY_USE_LOG_WIDTH_SCALE, old, useLogWidthScale);
  }

  public int getEdgeAlpha() {
    return edgeAlpha;
  }

  public int getDirectionMarkerAlpha() {
    return directionMarkerAlpha;
  }

  public double getEdgeWeightFilterMin() {
    return edgeWeightFilterMin;
  }

  public double getEdgeWeightFilterMax() {
    return edgeWeightFilterMax;
  }

  public double getEdgeLengthFilterMin() {
    return edgeLengthFilterMin;
  }

  public double getEdgeLengthFilterMax() {
    return edgeLengthFilterMax;
  }

  @Deprecated
  public boolean getAutoAdjustEdgeColorScale() {
    return autoAdjustEdgeColorScale;
  }

  public double getMaxEdgeWidth() {
    return maxEdgeWidth;
  }

  public void setDirectionMarkerAlpha(int edgeMarkerAlpha) {
    int old = this.directionMarkerAlpha;
    this.directionMarkerAlpha = edgeMarkerAlpha;
    changes.firePropertyChange(PROPERTY_DIRECTION_MARKER_ALPHA, old, edgeMarkerAlpha);
  }

  public void setEdgeWeightFilterMin(double valueFilterMin) {
    if (this.edgeWeightFilterMin != valueFilterMin) {
      double old = this.edgeWeightFilterMin;
      this.edgeWeightFilterMin = valueFilterMin;
      changes.firePropertyChange(PROPERTY_VALUE_FILTER_MIN, old, valueFilterMin);
    }
  }

  public void setEdgeWeightFilterMax(double valueFilterMax) {
    if (this.edgeWeightFilterMax != valueFilterMax) {
      double old = this.edgeWeightFilterMax;
      this.edgeWeightFilterMax = valueFilterMax;
      changes.firePropertyChange(PROPERTY_VALUE_FILTER_MAX, old, valueFilterMax);
    }
  }

  public void setEdgeLengthFilterMin(double edgeLengthFilterMin) {
    double old = this.edgeLengthFilterMin;
    this.edgeLengthFilterMin = edgeLengthFilterMin;
    changes.firePropertyChange(PROPERTY_EDGE_LENGTH_FILTER_MIN, old, edgeLengthFilterMin);
  }

  public void setEdgeLengthFilterMax(double edgeLengthFilterMax) {
    double old = this.edgeLengthFilterMax;
    this.edgeLengthFilterMax = edgeLengthFilterMax;
    changes.firePropertyChange(PROPERTY_EDGE_LENGTH_FILTER_MAX, old, edgeLengthFilterMax);
  }

  public void setAutoAdjustEdgeColorScale(boolean autoAdjustEdgeColorScale) {
    boolean old = this.autoAdjustEdgeColorScale;
    this.autoAdjustEdgeColorScale = autoAdjustEdgeColorScale;
    changes.firePropertyChange(PROPERTY_AUTO_ADJUST_EDGE_COLOR_SCALE, old, autoAdjustEdgeColorScale);
  }

  public void setMaxEdgeWidth(double maxEdgeWidth) {
    double old = this.maxEdgeWidth;
    this.maxEdgeWidth = maxEdgeWidth;
    changes.firePropertyChange(PROPERTY_MAX_EDGE_WIDTH, old, maxEdgeWidth);
  }

  public void setEdgeAlpha(int edgeAlpha) {
    int old = this.edgeAlpha;
    this.edgeAlpha = edgeAlpha;
    changes.firePropertyChange(PROPERTY_EDGE_ALPHA, old, edgeAlpha);
  }

  public boolean getShowNodes() {
    return showNodes;
  }

  public void setShowNodes(boolean value) {
    if (showNodes != value) {
      showNodes = value;
      changes.firePropertyChange(PROPERTY_SHOW_NODES, !value, value);
    }
  }

  public double getNodeSize() {
    return nodeSize;
  }

  public void setNodeSize(double size) {
    if (nodeSize != size) {
      double old = nodeSize;
      nodeSize = size;
      changes.firePropertyChange(PROPERTY_NODE_SIZE, old, nodeSize);
    }
  }

  public boolean getShowDirectionMarkers() {
    return showDirectionMarkers;
  }

  public void setShowDirectionMarkers(boolean value) {
    if (showDirectionMarkers != value) {
      showDirectionMarkers = value;
      changes.firePropertyChange(PROPERTY_SHOW_DIRECTION_MARKERS, !value, value);
    }
  }

  public boolean getShowSelfLoops() {
    return showSelfLoops;
  }

  public void setShowSelfLoops(boolean value) {
    if (showSelfLoops != value) {
      showSelfLoops = value;
      changes.firePropertyChange(PROPERTY_SHOW_SELF_LOOPS, !value, value);
    }
  }

  public boolean getFillEdgesWithGradient() {
    return fillEdgesWithGradient;
  }

  public void setFillEdgesWithGradient(boolean value) {
    if (fillEdgesWithGradient != value) {
      fillEdgesWithGradient = value;
      changes.firePropertyChange(PROPERTY_FILL_EDGES_WITH_GRADIENT, !value, value);
    }
  }

  public boolean getUseProportionalDirectionMarkers() {
    return useProportionalDirectionMarkers;
  }

  public void setUseProportionalDirectionMarkers(boolean value) {
    if (useProportionalDirectionMarkers != value) {
      useProportionalDirectionMarkers = value;
      changes.firePropertyChange(PROPERTY_USE_PROPORTIONAL_DIRECTION_MARKERS, !value, value);
    }
  }

  public double getDirectionMarkerSize() {
    return directionMarkerSize;
  }

  public MapBackgroundImage getMapBackgroundImage() {
    return mapBackgroundImage;
  }

  /**
   * @param markerSize
   *          A double value between 0 and 0.5. When useProportionalDirectionMarkers is true, the
   *          size of the marker for a certain edge E will be {@code markerSize * lengthOf(E)}.
   *          Otherwise it's {@code markerSize * lengthOfTheLongestEdge}
   */
  public void setDirectionMarkerSize(double markerSize) {
    if (markerSize < 0 || markerSize > .5) {
      throw new IllegalArgumentException(
          "Direction marker size must be between 0.0 and 0.5: attempted to set " + markerSize);
    }
    if (this.directionMarkerSize != markerSize) {
      double old = directionMarkerSize;
      this.directionMarkerSize = markerSize;
      changes.firePropertyChange(PROPERTY_DIRECTION_MARKER_SIZE, old, markerSize);
    }
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    changes.addPropertyChangeListener(listener);
  }

  public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    changes.addPropertyChangeListener(propertyName, listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    changes.removePropertyChangeListener(listener);
  }


  public double normalize(double value, boolean useLogValue) {
    SeqStat ws = getValueStat();
    if (useLogValue) {
      return ws.normalizer().normalizeLogAroundZero(value);
    } else {
      return ws.normalizer().normalizeAroundZero(value);
    }
  }

  public double normalizeForColorScale(double value) {
    return normalize(value, useLogColorScale);
  }

  public double normalizeForWidthScale(double value) {
    return normalize(value, useLogWidthScale);
  }

  public ForceDirectedBundlerParameters createForceDirectedBundlerParameters(String weightAttr) {
    ForceDirectedBundlerParameters params =
      new ForceDirectedBundlerParameters(flowMapGraph, weightAttr);

    params.setUpdateViewAfterEachStep(
        config.getBoolOrElse(VIEWCONF_BUNDLING_UPDATE_VIEW_AFTER_EACH_STEP, true));
    params.setNumCycles(
        config.getIntOrElse(VIEWCONF_BUNDLING_NUM_OF_CYCLES, params.getNumCycles()));
    params.setK(
        config.getDoubleOrElse(VIEWCONF_BUNDLING_EDGE_STIFFNESS, params.getK()));
    params.setEdgeCompatibilityThreshold(
        config.getDoubleOrElse(VIEWCONF_BUNDLING_COMPATIBILITY_THRESHOLD,
            params.getEdgeCompatibilityThreshold()));
    params.setEdgeCompatibilityThreshold(
        config.getDoubleOrElse(VIEWCONF_BUNDLING_COMPATIBILITY_THRESHOLD,
            params.getEdgeCompatibilityThreshold()));
    params.setS(config.getDoubleOrElse(VIEWCONF_BUNDLING_STEP_SIZE, params.getS()));
    params.setStepDampingFactor(
        config.getDoubleOrElse(VIEWCONF_BUNDLING_STEP_DAMPING_FACTOR,
            params.getStepDampingFactor()));
    params.setI(config.getIntOrElse(VIEWCONF_BUNDLING_STEPS_IN_1ST_CYCLE, params.getI()));
    params.setUseSimpleCompatibilityMeasure(
        config.getBoolOrElse(VIEWCONF_BUNDLING_SIMPLE_COMPATIBILITY_MEASURE,
            params.getUseSimpleCompatibilityMeasure()));

    return params;
  }

  public ViewConfig getViewConfig() {
    return config;
  }

}
