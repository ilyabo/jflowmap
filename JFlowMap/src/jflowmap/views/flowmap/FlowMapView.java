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

import java.awt.BorderLayout;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.List;

import javax.swing.JComponent;

import jflowmap.AbstractCanvasView;
import jflowmap.FlowMapColorSchemes;
import jflowmap.FlowMapGraph;
import jflowmap.data.FlowMapStats;
import jflowmap.data.GraphMLDatasetSpec;
import jflowmap.geo.MapProjection;
import jflowmap.models.map.GeoMap;
import jflowmap.ui.ControlPanel;
import jflowmap.views.ColorCodes;
import jflowmap.views.IFlowMapColorScheme;
import jflowmap.views.map.PGeoMap;

import org.apache.log4j.Logger;

import com.google.common.collect.Iterables;

/**
 * @author Ilya Boyandin
 */
public class FlowMapView extends AbstractCanvasView {

  private static final long serialVersionUID = -1898747650184999568L;

  public static Logger logger = Logger.getLogger(FlowMapView.class);

  private ControlPanel controlPanel;
  private VisualFlowMap visualFlowMap;
  private IFlowMapColorScheme colorScheme = FlowMapColorSchemes.LIGHT.getScheme();

  public static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("#,##0");

  public static final String VIEW_CONFIG_PROP_WEIGHT_FILTER_MIN = "view.flowmap.weightFilterMin";
  public static final String VIEW_CONFIG_PROP_WEIGHT_FILTER_MAX = "view.flowmap.weightFilterMax";
  public static final String VIEW_CONFIG_PROP_LENGTH_FILTER_MIN = "view.flowmap.lengthFilterMin";
  public static final String VIEW_CONFIG_PROP_LENGTH_FILTER_MAX = "view.flowmap.lengthFilterMax";
  public static final String VIEW_CONFIG_PROP_COLOR_SCHEME = "view.flowmap.colorScheme";
  public static final String VIEW_CONFIG_PROP_FILL_EDGES_WITH_GRADIENT = "view.flowmap.fillEdgesWithGradient";
  public static final String VIEW_CONFIG_PROP_SHOW_DIRECTION_MARKERS = "view.flowmap.showDirectionMarkers";
  public static final String VIEW_CONFIG_PROP_SHOW_NODES = "view.flowmap.showNodes";
  public static final String VIEW_CONFIG_PROP_EDGE_WIDTH = "view.flowmap.edgeWidth";
  public static final String VIEW_CONFIG_PROP_EDGE_OPACITY = "view.flowmap.edgeOpacity";


  public FlowMapView(VisualFlowMapModel model, GeoMap areaMap, MapProjection proj, IFlowMapColorScheme colorScheme) {
    FlowMapGraph fmg = model.getFlowMapGraph();

    setVisualFlowMap(createVisualFlowMap(model, proj, Iterables.getLast(fmg.getEdgeWeightAttrs())));
    if (areaMap != null) {
      visualFlowMap.setAreaMap(new PGeoMap(visualFlowMap, areaMap, proj));
    }
    if (colorScheme != null) {
      setColorScheme(colorScheme);
    }
    controlPanel = new ControlPanel(this, fmg.getAttrSpec());
  }

  public FlowMapView(List<GraphMLDatasetSpec> datasetSpecs, boolean showControlPanel) {
//    if (visualFlowMap == null) {
//      return;  // an exception occured during the initialization
//    }
    if (datasetSpecs != null) {
      load(datasetSpecs.get(0));
      getVisualCanvas().getLayer().addChild(visualFlowMap);
    }

    if (showControlPanel) {
      controlPanel = new ControlPanel(this, getVisualFlowMap().getFlowMapGraph().getAttrSpec());
    }
  }

  @Override
  public JComponent getControls() {
    if (controlPanel != null) {
      return controlPanel.getPanel();
    } else {
      return null;
    }
  }

  @Override
  public String getControlsLayoutConstraint() {
    return BorderLayout.SOUTH;
  }

  public void load(GraphMLDatasetSpec dataset) {
    load(dataset, null);
  }

  /**
   * Use when the stats have to be induced and not calculated (e.g. when a global mapping over
   * a number of flow maps for small multiples must be used).
   * Otherwise, use {@link #load(GraphMLDatasetSpec)}.
   */
  public void load(GraphMLDatasetSpec dataset, FlowMapStats stats) {
    logger.info("> Loading flow map '" + dataset + "'");
    try {
      FlowMapGraph fmg = FlowMapGraph.loadGraphML(dataset, stats);

      VisualFlowMap visualFlowMap = createVisualFlowMap(
          new VisualFlowMapModel(fmg),
          dataset.getMapProjection(), fmg.getEdgeWeightAttrs().get(0));

      GeoMap areaMap = GeoMap.loadFor(dataset);

      if (areaMap != null) {
        visualFlowMap.setAreaMap(new PGeoMap(visualFlowMap, areaMap, dataset.getMapProjection()));
      }
      setVisualFlowMap(visualFlowMap);

    } catch (Exception ex) {
      logger.error("Couldn't load flow map " + dataset.getFilename(), ex);
      throw new RuntimeException("Couldn't load flow map '" + dataset.getFilename() + "':\n" + ex.getMessage());
    }
  }

  public void setSelectedFlowWeightAttr(String flowWeightAttr) {
    visualFlowMap.setSelectedFlowWeightAttr(flowWeightAttr);
  }

  public String getSelectedFlowWeightAttr() {
    return visualFlowMap.getSelectedFlowWeightAttr();
  }

  @Override
  public String getName() {
    return visualFlowMap.getName();
  }

  public void setColorScheme(IFlowMapColorScheme colorScheme) {
    this.colorScheme = colorScheme;
    getVisualCanvas().setBackground(colorScheme.get(ColorCodes.BACKGROUND));
    if (visualFlowMap != null) {
      visualFlowMap.updateColors();
    }
    if (controlPanel != null) {
      controlPanel.updateColorScheme();
    }
  }

  public IFlowMapColorScheme getColorScheme() {
    return colorScheme;
  }

  public Color getColor(ColorCodes code) {
    return colorScheme.get(code);
  }

  public VisualFlowMap createVisualFlowMap(VisualFlowMapModel model, MapProjection proj,
      String flowWeightAttr) {
    return new VisualFlowMap(this, model, true, proj, flowWeightAttr);
  }

  public VisualFlowMap getVisualFlowMap() {
    return visualFlowMap;
  }

  public void setVisualFlowMap(VisualFlowMap newFlowMap) {
    if (newFlowMap == visualFlowMap) {
      return;
    }
    if (visualFlowMap != null) {
      getVisualCanvas().getLayer().removeChild(visualFlowMap);
      visualFlowMap.removeNodesFromCamera();
    }
    getVisualCanvas().getLayer().addChild(newFlowMap);
    visualFlowMap = newFlowMap;
    newFlowMap.addNodesToCamera();
    if (controlPanel != null) {
      controlPanel.loadVisualFlowMap(newFlowMap);
    }
  }

  @Override
  public void fitInView() {
    visualFlowMap.fitInCameraView();
  }

}
