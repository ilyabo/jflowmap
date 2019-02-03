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

import javax.swing.JComponent;

import jflowmap.AbstractCanvasView;
import jflowmap.FlowMapGraph;
import jflowmap.geo.MapProjections;
import jflowmap.models.map.GeoMap;
import jflowmap.util.Pair;
import jflowmap.util.piccolo.PBoxLayoutNode;
import jflowmap.util.piccolo.PButton;
import jflowmap.util.piccolo.PNodes;
import jflowmap.util.piccolo.ZoomHandler;
import jflowmap.views.ColorCodes;
import jflowmap.views.IFlowMapColorScheme;
import jflowmap.views.Legend;
import jflowmap.views.PTooltip;
import jflowmap.views.VisualCanvas;
import jflowmap.views.flowstrates.ValueType;
import jflowmap.views.map.PGeoMap;

import org.apache.log4j.Logger;

import prefuse.data.Edge;

import com.google.common.collect.Lists;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventFilter;
import edu.umd.cs.piccolo.event.PPanEventHandler;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PDimension;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolox.util.PFixedWidthStroke;

/**
 * @author Ilya Boyandin
 */
public class FlowMapSmallMultipleView extends AbstractCanvasView {

  public static final String VIEWCONF_NUM_OF_COLUMNS = "view.flowMapSmallMultiple.numberOfColumns";
  public static Logger logger = Logger.getLogger(FlowMapSmallMultipleView.class);

  private List<VisualFlowMapLayer> layers;
  private final VisualFlowMapModel model;
  private final int numberOfColumns;

  public FlowMapSmallMultipleView(VisualFlowMapModel model, final GeoMap areaMap, final MapProjections proj,
      final IFlowMapColorScheme cs, int numberOfColumns) {

    this.model = model;
    this.numberOfColumns = numberOfColumns;

    FlowMapGraph fmg = model.getFlowMapGraph();

    VisualCanvas canvas = getVisualCanvas();
    canvas.setInteractingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
    canvas.setAnimatingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);

    final PTooltip tooltip = new PTooltip();

    layers = Lists.newArrayList();
    for (String attr : fmg.getEdgeWeightAttrs()) {
      final PCamera camera = new PCamera();
      final VisualFlowMap vfm = new VisualFlowMap(this, model, true, proj, attr, cs) {
        @Override
        public PCamera getCamera() {
          return camera;
        }
        @Override
        protected PTooltip createTooltip() {
          return tooltip;
        }
        @Override
        public void showTooltip(PNode component, Point2D pos) {
          Point2D p = new Point2D.Double(pos.getX(), pos.getY());
          getCamera().viewToLocal(p);
          super.showTooltip(component, p);
        }
      };

      canvas.getCamera().addChild(tooltip);

      if (areaMap != null) {
        vfm.setAreaMap(new PGeoMap(vfm, areaMap, proj));
      }
      VisualFlowMapLayer layer = new VisualFlowMapLayer(vfm, camera);
      layers.add(layer);

      vfm.addPropertyChangeListener(VisualFlowMap.PROPERTY_HIGHLIGHTED, new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
          for (VisualFlowMapLayer layer : layers) {
            if (layer.getVisualFlowMap() != vfm) {
              @SuppressWarnings("unchecked")
              Pair<Edge, Boolean> val = (Pair<Edge, Boolean>) evt.getNewValue();
              layer.getVisualFlowMap().setEdgeHighlighted(val.first(), val.second());
            }
          }
        }
      });

      canvas.getLayer().addChild(layer.getCamera());
    }

    final Legend legend;
    if (layers.size() > 0) {
      VisualFlowMapLayer layer1 = layers.get(0);
      final VisualFlowMap vfm = layer1.getVisualFlowMap();
      final PCamera cam = layer1.getCamera();
      legend = vfm.getVisualLegend();
      cam.addChild(legend);
      cam.addPropertyChangeListener(new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
          if (evt.getPropertyName() == PCamera.PROPERTY_VIEW_TRANSFORM) {
            if (logger.isDebugEnabled()) {
              PBounds fb = vfm.getVisualNodesBounds();
              PBounds b = cam.getViewBounds();
              double areap = (b.width * b.height) / (fb.width * fb.height / 100);
              logger.debug("View transform to " + b +
                  " (" + FlowMapView.NUMBER_FORMAT.format(areap) + "% of visible area)");
            }
          }
        }
      });
    } else {
      legend = null;
    }

    final PButton diffButton = new PButton("DIFF", true, new Font("Dialog", Font.PLAIN, 11));
    getCamera().addChild(diffButton);
    diffButton.addInputEventListener(new PBasicInputEventHandler() {
      @Override
      public void mouseClicked(PInputEvent event) {
        for (int i = 0; i < layers.size(); i++) {
          VisualFlowMap vfm = layers.get(i).getVisualFlowMap();
          if (diffButton.isPressed()) {
            vfm.setValueType(ValueType.DIFF);
          } else {
            vfm.setValueType(ValueType.VALUE);
          }
        }
      }
    });
    getVisualCanvas().getModeButtonsPanel().addChild(diffButton);


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

          if (legend != null) {
//            PBounds b = getCamera().getBoundsReference();
            PBounds lb = legend.getFullBoundsReference();
            PBoxLayoutNode mp = getVisualCanvas().getModeButtonsPanel();
            PBounds mpb = mp.getFullBoundsReference();
            PNodes.setPosition(mp, lb.getMaxX() - mpb.width, lb.getMaxY() + 4);
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
      "valueType="+ (layers.size() > 0 ? layers.get(0).getVisualFlowMap().getValueType() : "") +
    "]";
  }

  @Override
  public JComponent getControls() {
//    return new JPanel();
    return null;
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
    private final VisualFlowMap visualFlowMap;
    private final PCamera camera;
    private final PPath pp;

    public VisualFlowMapLayer(VisualFlowMap visualFlowMap, PCamera camera) {
      this.visualFlowMap = visualFlowMap;
      this.camera = camera;
      this.camera.addLayer(this);
//      this.camera.addChild(visualFlowMap.getTooltipBox());

      // border
      pp = new PPath(new PBounds(0, 0, 1, 1));
      pp.setStroke(new PFixedWidthStroke(2));
      pp.setStrokePaint(Color.gray);
      camera.addChild(pp);

      visualFlowMap.setFlowWeightAttrLabelVisibile(true);

      addChild(visualFlowMap);
    }

    public VisualFlowMap getVisualFlowMap() {
      return visualFlowMap;
    }

    public PCamera getCamera() {
      return camera;
    }

    public void adjustBounds(double x, double y, double w, double h) {
      PBounds viewBounds = getCamera().getViewBounds();
      getCamera().setBounds(x, y, w, h);
      pp.setBounds(x, y, w, h);
      visualFlowMap.updateFlowWeightAttrLabel();
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
    int numRows = (int) Math.ceil(((double) size) / numColumns);

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
