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

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import jflowmap.AbstractCanvasView;
import jflowmap.DatasetSpec;
import jflowmap.FlowMapColorSchemes;
import jflowmap.FlowMapGraph;
import jflowmap.data.FlowMapStats;
import jflowmap.geo.MapProjections;
import jflowmap.models.map.AreaMap;
import jflowmap.ui.ControlPanel;
import jflowmap.views.ColorCodes;
import jflowmap.views.ColorScheme;

import org.apache.log4j.Logger;

/**
 * @author Ilya Boyandin
 */
public class FlowMapView extends AbstractCanvasView {

  private static final long serialVersionUID = -1898747650184999568L;

  public static Logger logger = Logger.getLogger(FlowMapView.class);

  private ControlPanel controlPanel;
  private VisualFlowMap visualFlowMap;
  private ColorScheme colorScheme = FlowMapColorSchemes.LIGHT.getScheme();

  public static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("#,##0");

  public FlowMapView(FlowMapGraph flowMapGraph, AreaMap areaMap) {
    setVisualFlowMap(createVisualFlowMap(flowMapGraph));
    if (areaMap != null) {
      visualFlowMap.setAreaMap(new VisualAreaMap(visualFlowMap, areaMap,
          MapProjections.NONE));
    }
  }

  public FlowMapView(List<DatasetSpec> datasetSpecs, boolean showControlPanel) {
    if (datasetSpecs != null) {
      load(datasetSpecs.get(0));
      getVisualCanvas().getLayer().addChild(visualFlowMap);
    }

    if (showControlPanel) {
      controlPanel = new ControlPanel(this, datasetSpecs);
    }
  }

  @Override
  public JComponent getControls() {
    return controlPanel.getPanel();
  }

  public void load(DatasetSpec dataset) {
    load(dataset, null);
  }

  /**
   * Use when the stats have to be induced and not calculated (e.g. when a global mapping over
   * a number of flow maps for small multiples must be used).
   * Otherwise, use {@link #load(DatasetSpec)}.
   */
  public void load(DatasetSpec dataset, FlowMapStats stats) {
    logger.info("> Loading flow map '" + dataset + "'");
    try {
      FlowMapGraph flowMapGraph = FlowMapGraph.loadGraphML(dataset.getFilename(),
          dataset.getAttrsSpec(), stats);

      VisualFlowMap visualFlowMap = createVisualFlowMap(flowMapGraph);
      if (dataset.getAreaMapFilename() != null) {
        AreaMap areaMap = AreaMap.load(dataset.getAreaMapFilename());
        visualFlowMap.setAreaMap(
            new VisualAreaMap(visualFlowMap, areaMap, MapProjections.NONE));
      }
      setVisualFlowMap(visualFlowMap);

    } catch (Throwable th) {
      logger.error("Couldn't load flow map " + dataset.getFilename(), th);
      JOptionPane.showMessageDialog(this.getViewComponent(),
          "Couldn't load flow map '"  + dataset.getFilename() + "': [" +
          th.getClass().getSimpleName()+ "] " + th.getMessage());
    }
  }

  @Override
  public String getName() {
    return visualFlowMap.getName();
  }

  public void setColorScheme(ColorScheme colorScheme) {
    this.colorScheme = colorScheme;
    getVisualCanvas().setBackground(colorScheme.get(ColorCodes.BACKGROUND));
    if (visualFlowMap != null) {
      visualFlowMap.updateColors();
    }
    if (controlPanel != null) {
      controlPanel.updateColorScheme();
    }
  }

  public ColorScheme getColorScheme() {
    return colorScheme;
  }

  public Color getColor(ColorCodes code) {
    return colorScheme.get(code);
  }

  public VisualFlowMap createVisualFlowMap(FlowMapGraph flowMapGraph) {
    return new VisualFlowMap(this, flowMapGraph, true);
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
