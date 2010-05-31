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

package jflowmap;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.text.DecimalFormat;
import java.util.List;

import javax.swing.JOptionPane;

import jflowmap.data.FlowMapStats;
import jflowmap.models.map.AreaMap;
import jflowmap.ui.ControlPanel;
import jflowmap.visuals.ColorCodes;
import jflowmap.visuals.ColorScheme;
import jflowmap.visuals.flowmap.VisualAreaMap;
import jflowmap.visuals.flowmap.VisualFlowMap;

import org.apache.log4j.Logger;

/**
 * @author Ilya Boyandin
 */
public class JFlowMap extends JView {

  private static final long serialVersionUID = -1898747650184999568L;

  public static Logger logger = Logger.getLogger(JFlowMap.class);

  private ControlPanel controlPanel;
  private VisualFlowMap visualFlowMap;
  private ColorScheme colorScheme;

  public static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("#,##0");

  public JFlowMap(FlowMapGraph flowMapGraph, AreaMap areaMap) {
    init();

    controlPanel = null;

    setVisualFlowMap(createVisualFlowMap(flowMapGraph));
    if (areaMap != null) {
      visualFlowMap.setAreaMap(new VisualAreaMap(visualFlowMap, areaMap));
    }
  }

  public JFlowMap(List<DatasetSpec> datasetSpecs, boolean showControlPanel) {
    init();

    if (datasetSpecs != null) {
      load(datasetSpecs.get(0));
      getVisualCanvas().getLayer().addChild(visualFlowMap);
    }

    if (showControlPanel) {
      controlPanel = new ControlPanel(this, datasetSpecs);
      add(controlPanel.getPanel(), BorderLayout.SOUTH);
    } else {
      controlPanel = null;
    }
  }

  private void init() {
    setColorScheme(FlowMapColorSchemes.LIGHT.getScheme());
//    setColorScheme(FlowMapColorSchemes.LIGHT_BLUE__COLOR_BREWER.getScheme());
//    setColorScheme(FlowMapColorSchemes.DARK.getScheme());
//    setColorScheme(FlowMapColorSchemes.DARK.getScheme());
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
      FlowMapGraph flowMapGraph = FlowMapGraph.loadGraphML(dataset.getFilename(), dataset.getAttrsSpec(), stats);

      VisualFlowMap visualFlowMap = createVisualFlowMap(flowMapGraph);
      if (dataset.getAreaMapFilename() != null) {
        AreaMap areaMap = AreaMap.load(dataset.getAreaMapFilename());
        visualFlowMap.setAreaMap(new VisualAreaMap(visualFlowMap, areaMap));
      }
      setVisualFlowMap(visualFlowMap);

    } catch (Throwable th) {
      logger.error("Couldn't load flow map " + dataset.getFilename(), th);
      JOptionPane.showMessageDialog(this,
          "Couldn't load flow map '"  + dataset.getFilename() + "': [" + th.getClass().getSimpleName()+ "] " + th.getMessage());
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

  public Frame getParentFrame() {
    Component parent = this;
    while (parent != null) {
      parent = parent.getParent();
      if (parent instanceof Frame) {
        return (Frame) parent;
      }
    }
    return null;
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
