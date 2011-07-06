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
import java.awt.Font;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import jflowmap.AbstractCanvasView;
import jflowmap.FlowMapGraph;
import jflowmap.geo.MapProjections;
import jflowmap.models.map.GeoMap;
import jflowmap.util.piccolo.ZoomHandler;
import jflowmap.views.ColorCodes;
import jflowmap.views.IFlowMapColorScheme;
import jflowmap.views.VisualCanvas;
import jflowmap.views.map.PGeoMap;

import com.google.common.collect.Lists;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventFilter;
import edu.umd.cs.piccolo.event.PPanEventHandler;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PDimension;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolox.util.PFixedWidthStroke;

/**
 * @author Ilya Boyandin
 */
public class FlowMapSmallMultipleView extends AbstractCanvasView {

  private final List<VisualFlowMapLayer> layers;
  private final VisualFlowMapModel model;
  private final int numberOfColumns = 7;


  public FlowMapSmallMultipleView(VisualFlowMapModel model, GeoMap areaMap, MapProjections proj,
      IFlowMapColorScheme cs) {

    this.model = model;

    FlowMapGraph fmg = model.getFlowMapGraph();
    VisualCanvas canvas = getVisualCanvas();

    canvas.setInteractingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
    canvas.setAnimatingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);

    layers = Lists.newArrayList();
    for (String attr : fmg.getEdgeWeightAttrs()) {
      final PCamera camera = new PCamera();
      VisualFlowMap vfm = new VisualFlowMap(this, model, true, proj, attr, cs) {
        @Override
        public PCamera getCamera() {
          return camera;
        }
      };
      if (areaMap != null) {
        vfm.setAreaMap(new PGeoMap(vfm, areaMap, proj));
      }
      VisualFlowMapLayer layer = new VisualFlowMapLayer(vfm, camera);
      layers.add(layer);

      canvas.getLayer().addChild(layer.getCamera());
    }

    canvas.setBackground(cs.get(ColorCodes.BACKGROUND));
    canvas.setAutoFitOnBoundsChange(false);
    canvas.setPanEventHandler(createPanHandler());
    canvas.setZoomHandler(createZoomHandler());

    layoutChildren();
    getCamera().addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == PCamera.PROPERTY_BOUNDS) {
          layoutChildren();
//          fitInView();
        }
      }
    });

    fitInView();

  }

  private ZoomHandler createZoomHandler() {
    ZoomHandler zoomHandler = new ZoomHandler() {
      @Override
      protected void zoom(PCamera c, double scale, Point2D position) {
        scale = checkScaleConstraints(c.getViewScale(), scale);
        for (VisualFlowMapLayer layer : layers) {
          PCamera cam = layer.getCamera();
          cam.scaleViewAboutPoint(scale, position.getX(), position.getY());
        }
      }
    };
    zoomHandler.setEventFilter(new PInputEventFilter() {
      @Override
      public boolean acceptsEvent(PInputEvent event, int type) {
        return !event.isControlDown() && // shouldn't pan when using lasso
            (event.getCamera() != getVisualCanvas().getCamera());
      }
    });
    return zoomHandler;
  }

  private PPanEventHandler createPanHandler() {
    PPanEventHandler panHandler = new PPanEventHandler() {
      @Override
      protected void pan(final PInputEvent event) {
        final Point2D l = event.getPosition();
        final PDimension d = event.getDelta();
        for (VisualFlowMapLayer layer : layers) {
          final PCamera c = layer.getCamera();
          if (c.getViewBounds().contains(l)) {
            c.translateView(d.getWidth(), d.getHeight());
          }
        }
      }
    };
    panHandler.setAutopan(false);

    panHandler.setEventFilter(new PInputEventFilter() {
      @Override
      public boolean acceptsEvent(PInputEvent event, int type) {
        return !event.isControlDown() && // shouldn't pan when using lasso
            (event.getCamera() != getVisualCanvas().getCamera());
      }
    });
    return panHandler;
  }

  static class VisualFlowMapLayer extends PLayer {
    private final Font CAPTION_FONT = new Font("Arial", Font.BOLD, 20);
    private final VisualFlowMap visualFlowMap;
    private final PCamera camera;
    private final PPath pp;
    private final PText caption;

    public VisualFlowMapLayer(VisualFlowMap visualFlowMap, PCamera camera) {
      this.visualFlowMap = visualFlowMap;
      this.camera = camera;
      this.camera.addLayer(this);
      this.camera.addChild(visualFlowMap.getTooltipBox());

      // border
      pp = new PPath(new PBounds(0, 0, 1, 1));
      pp.setStroke(new PFixedWidthStroke(2));
      pp.setStrokePaint(Color.gray);
      camera.addChild(pp);

      caption = new PText(visualFlowMap.getFlowWeightAttr());
      caption.setFont(CAPTION_FONT);
      caption.setTextPaint(Color.white);
      caption.setTransparency(0.3f);
      camera.addChild(caption);

      addChild(visualFlowMap);
    }

    public VisualFlowMap getVisualFlowMap() {
      return visualFlowMap;
    }

    public PCamera getCamera() {
      return camera;
    }

    public void adjustBounds(double x, double y, double w, double h) {
      PBounds viewBounds = camera.getViewBounds();
      getCamera().setBounds(x, y, w, h);
      pp.setBounds(x, y, w, h);
      caption.setBounds(x + 3, y + (h - caption.getHeight() - 2), caption.getWidth(), caption.getHeight());
      if (!viewBounds.isEmpty()) {
        camera.setViewBounds(viewBounds);
      }
    }
  }


  @Override
  public void fitInView() {
    for (VisualFlowMapLayer layer : layers) {
      layer.getVisualFlowMap().fitInCameraView();
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
            layer.adjustBounds(vbounds.getX() + w * j, vbounds.getY() + h * i, w, h);
          }
          count++;
        }
      }
    }
  }


}
