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
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import jflowmap.AbstractCanvasView;
import jflowmap.FlowMapGraph;
import jflowmap.geo.MapProjections;
import jflowmap.models.map.GeoMap;
import jflowmap.views.ColorCodes;
import jflowmap.views.IFlowMapColorScheme;
import jflowmap.views.VisualCanvas;
import jflowmap.views.map.PGeoMap;

import com.google.common.collect.Lists;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolox.util.PFixedWidthStroke;

/**
 * @author Ilya Boyandin
 */
public class FlowMapSmallMultipleView extends AbstractCanvasView {

  private final List<VisualFlowMapLayer> layers;
  private final VisualFlowMapModel model;
  private final int numberOfColumns = 5;

  public FlowMapSmallMultipleView(VisualFlowMapModel model, GeoMap areaMap, MapProjections proj,
      IFlowMapColorScheme cs) {

    this.model = model;

    FlowMapGraph fmg = model.getFlowMapGraph();
    VisualCanvas canvas = getVisualCanvas();

    layers = Lists.newArrayList();
    for (String attr : fmg.getEdgeWeightAttrs()) {
      VisualFlowMap vfm = new VisualFlowMap(this, model, true, proj, attr, cs);
      if (areaMap != null) {
        vfm.setAreaMap(new PGeoMap(vfm, areaMap, proj));
      }
      VisualFlowMapLayer layer = new VisualFlowMapLayer(vfm);
      layers.add(layer);

      canvas.getLayer().addChild(layer.getCamera());
    }

    canvas.setBackground(cs.get(ColorCodes.BACKGROUND));
    canvas.setAutoFitOnBoundsChange(false);

    layoutChildren();
    getCamera().addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == PCamera.PROPERTY_BOUNDS) {
          layoutChildren();
//          fitInView();
        }
      }
    });

  }


  static class VisualFlowMapLayer extends PLayer {
    private final VisualFlowMap visualFlowMap;
    private final PCamera camera;
    private final PPath pp;

    public VisualFlowMapLayer(VisualFlowMap visualFlowMap) {
      this.visualFlowMap = visualFlowMap;
      this.camera = new PCamera();
      this.camera.addLayer(this);
      visualFlowMap.addNodesToCamera();
      pp = new PPath(new PBounds(0, 0, 1, 1));
      pp.setStroke(new PFixedWidthStroke(2));
      pp.setStrokePaint(Color.gray);
      camera.addChild(pp);
      addChild(visualFlowMap);
    }

    public VisualFlowMap getVisualFlowMap() {
      return visualFlowMap;
    }

    public PCamera getCamera() {
      return camera;
    }

    public void adjustBounds(double x, double y, double w, double h) {
//      PBounds viewBounds = camera.getViewBounds();
      getCamera().setBounds(x, y, w, h);
      pp.setBounds(x, y, w, h);
//      camera.setViewBounds(viewBounds);
    }

  }


  @Override
  public String getName() {
    return model.getFlowMapGraph().getId();
  }


  private void layoutChildren() {
    Rectangle2D vbounds = getCamera().getViewBounds();

    int size = layers.size();
    int numColumns = Math.min(numberOfColumns, size);
    int numRows = (int) Math.ceil(((double)size) / numColumns);

    double w = vbounds.getWidth() / numColumns;
    double h = vbounds.getHeight() / numRows;

    {
      int count = 0;
      for (int i = 0; i < numRows; i++) {
        for (int j = 0; j < numColumns; j++) {
          if (count < size) {
            VisualFlowMapLayer layer = layers.get(count);
            layer.adjustBounds(w * j, h * i, w, h);
          }
          count++;
        }
      }
    }
  }


}
