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

import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;

import javax.swing.JComponent;

import jflowmap.AbstractCanvasView;
import jflowmap.FlowMapGraph;
import jflowmap.data.ViewConfig;
import jflowmap.geo.MapProjection;
import jflowmap.models.map.GeoMap;
import jflowmap.ui.ControlPanel;
import jflowmap.util.piccolo.PBoxLayoutNode;
import jflowmap.util.piccolo.PButton;
import jflowmap.util.piccolo.PNodes;
import jflowmap.views.ColorCodes;
import jflowmap.views.IFlowMapColorScheme;
import jflowmap.views.VisualCanvas;
import jflowmap.views.flowstrates.ValueType;
import jflowmap.views.map.PGeoMap;

import org.apache.log4j.Logger;

import com.google.common.collect.Iterables;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * @author Ilya Boyandin
 */
public class FlowMapView extends AbstractCanvasView {

  private static final long serialVersionUID = -1898747650184999568L;

  public static Logger logger = Logger.getLogger(FlowMapView.class);

  private final ControlPanel controlPanel;
  private VisualFlowMap visualFlowMap;

  private final ViewConfig viewConfig;

//  public static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("0.#####");
  public static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("#,##0.#####");

  public FlowMapView(VisualFlowMapModel model, GeoMap areaMap, MapProjection proj,
      IFlowMapColorScheme cs, ViewConfig config) {
    this.viewConfig = config;

    FlowMapGraph fmg = model.getFlowMapGraph();

    setVisualFlowMap(createVisualFlowMap(model, proj, Iterables.getLast(fmg.getEdgeWeightAttrs()), cs));
    if (areaMap != null) {
      visualFlowMap.setAreaMap(new PGeoMap(visualFlowMap, areaMap, proj));
    }
    if (cs != null) {
      setColorScheme(cs);
    }

    VisualCanvas canvas = getVisualCanvas();
    canvas.setInteractingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
    canvas.setAnimatingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);

    visualFlowMap.setFlowWeightAttrLabelVisibile(
      config.getBoolOrElse(VisualFlowMapModel.VIEWCONF_SHOW_FLOW_WEIGHT_ATTR_LABEL, false)
    );

    addDiffButton(visualFlowMap);

    controlPanel = new ControlPanel(this, fmg.getAttrSpec());

    getCamera().addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == PCamera.PROPERTY_VIEW_TRANSFORM) {
          if (logger.isDebugEnabled()) {
            PBounds fb = getVisualFlowMap().getVisualNodesBounds();
            PBounds b = getCamera().getViewBounds();
            double areap = (b.width * b.height) / (fb.width * fb.height / 100);
            logger.debug("View transform to " + b +
                " (" + FlowMapView.NUMBER_FORMAT.format(areap) + "% of visible area)");
          }
        }
      }
    });

    fitInView();
  }

  @Override
  public String getSpec() {
    return getClass().getSimpleName() + "[" +
      "name='" + getName() + "', " +
      "valueType="+visualFlowMap.getValueType() +
    "]";
  }

  public static void addDiffButton(final VisualFlowMap visualFlowMap) {
    final PCamera cam = visualFlowMap.getCamera();
    final VisualCanvas canvas = visualFlowMap.getVisualCanvas();

    final PButton diffButton = new PButton("DIFF", true, new Font("Dialog", Font.PLAIN, 11));
    cam.addChild(diffButton);

    diffButton.addInputEventListener(new PBasicInputEventHandler() {
      @Override
      public void mouseClicked(PInputEvent event) {
        if (diffButton.isPressed()) {
          visualFlowMap.setValueType(ValueType.DIFF);
        } else {
          visualFlowMap.setValueType(ValueType.VALUE);
        }
      }
    });

    canvas.getModeButtonsPanel().addChild(diffButton);
    cam.addPropertyChangeListener(PCamera.PROPERTY_BOUNDS, new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        PBounds lb = visualFlowMap.getVisualLegend().getFullBoundsReference();
        PBoxLayoutNode mp = canvas.getModeButtonsPanel();
        PBounds mpb = mp.getFullBoundsReference();
        PNodes.setPosition(mp, lb.getMaxX() - mpb.width, lb.getMaxY() + 4);
      }
    });
  }

  @Override
  public JComponent getControls() {
    if (controlPanel != null) {
      return controlPanel.getPanel();
    } else {
      return null;
    }
  }

  public void setSelectedFlowWeightAttr(String flowWeightAttr) {
    visualFlowMap.setFlowWeightAttr(flowWeightAttr);
  }

  @Override
  public String getName() {
    if (viewConfig == null) {
      return "";
    }
    return viewConfig.getName();
  }

  public void setColorScheme(IFlowMapColorScheme cs) {
    if (visualFlowMap != null) {
      visualFlowMap.setColorScheme(cs);
    }
    if (controlPanel != null) {
      controlPanel.updateColorScheme();
    }
    getVisualCanvas().setBackground(cs.get(ColorCodes.BACKGROUND));
  }

  public IFlowMapColorScheme getColorScheme() {
    return visualFlowMap.getColorScheme();
  }

  public VisualFlowMap createVisualFlowMap(VisualFlowMapModel model, MapProjection proj,
      String flowWeightAttr, IFlowMapColorScheme cs) {
    return new VisualFlowMap(this, model, true, proj, flowWeightAttr, cs);
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
