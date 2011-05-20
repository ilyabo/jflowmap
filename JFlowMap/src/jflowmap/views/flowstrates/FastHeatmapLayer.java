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

package jflowmap.views.flowstrates;

import java.awt.Color;
import java.awt.Insets;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.List;

import jflowmap.FlowEndpoint;
import jflowmap.FlowMapGraph;
import jflowmap.util.piccolo.PNodes;
import jflowmap.util.piccolo.PiccoloUtils;
import prefuse.data.Edge;
import prefuse.data.Node;
import at.fhjoanneum.cgvis.data.IColorForValue;
import at.fhjoanneum.cgvis.data.IDataValues;
import at.fhjoanneum.cgvis.plots.FloatingLabelsNode;
import at.fhjoanneum.cgvis.plots.FloatingLabelsNode.LabelIterator;
import at.fhjoanneum.cgvis.plots.mosaic.MosaicPlotNode;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * @author Ilya Boyandin
 */
public class FastHeatmapLayer extends TemporalViewLayer {

  private static final Color FLOATING_LABELS_BG = new Color(255, 255, 255, 230);
  private static final int MAX_NODE_NAME_LENGTH = 18;
  private MosaicPlotNode heatmapNode;
  private final IColorForValue colorForValue;
  private final FloatingLabelsNode attrLabelsNode;
  private final FloatingLabelsNode originLabelsNode;

  private final FloatingLabelsNode destLabelsNode;

  public FastHeatmapLayer(FlowstratesView flowstratesView) {
    super(flowstratesView);
    colorForValue = createColorForValue();

    renew();

    getCamera().setComponent(getFlowstratesView().getVisualCanvas());

    originLabelsNode = createFloatingLabels(false, createNodeLabelIterator(FlowEndpoint.ORIGIN), true);
    destLabelsNode = createFloatingLabels(false, createNodeLabelIterator(FlowEndpoint.DEST), false);
    attrLabelsNode = createFloatingLabels(true, createAttrsLabelIterator(), false);

    originLabelsNode.addDisjointNode(attrLabelsNode);
    destLabelsNode.addDisjointNode(attrLabelsNode);

    getCamera().addPropertyChangeListener(PCamera.PROPERTY_VIEW_TRANSFORM, new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        adjustFloatingLabelsPositions();
      }
    });
  }

  private FloatingLabelsNode createFloatingLabels(
      boolean isHorizontal, LabelIterator it, boolean anchorLabelsToEnd) {
    FloatingLabelsNode node = new FloatingLabelsNode(isHorizontal, it);
    node.setAnchorLabelsToEnd(anchorLabelsToEnd);
    node.setPaint(FLOATING_LABELS_BG);
    node.setPickable(false);
    getCamera().addChild(node);
    return node;
  }

  @Override
  public void renew() {
    removeAllChildren();
//    getCamera().removeAllChildren();

    IDataValues data = getDataValues();
    FlowstratesView fs = getFlowstratesView();
    heatmapNode = new MosaicPlotNode(data, fs.getVisualCanvas(), colorForValue);

    Color missingColor = fs.getStyle().getMissingValueColor();
    heatmapNode.setMissingValueColor1(missingColor);
    heatmapNode.setMissingValueColor2(missingColor);

    addChild(heatmapNode);
  }

  private IColorForValue createColorForValue() {
    return new IColorForValue() {
      @Override
      public Color getColorForValue(double value) {
        return getFlowstratesView().getColorFor(value);
      }
    };
  }

  private IDataValues getDataValues() {
    return new IDataValues() {
      FlowstratesView fs = getFlowstratesView();
      FlowMapGraph fmg = fs.getFlowMapGraph();
      List<String> attrs = fmg.getEdgeWeightAttrs();
      List<Edge> edges = fs.getVisibleEdges();
      @Override
      public int getSize() {
        return edges.size();
      }
      @Override
      public int getDimension() {
        return fmg.getEdgeWeightAttrsCount();
      }
      @Override
      public double getValue(int element, int attribute) {
        return getFlowstratesView().getValue(edges.get(element), attrs.get(attribute));
      }
    };
  }


  @Override
  public void updateColors() {
    renew();
  }

  @Override
  public void resetWeightAttrTotals() {
  }

  private void adjustFloatingLabelsPositions() {
    PBounds cb = getCamera().getBoundsReference();

    PBounds hb = heatmapNode.getBounds();
    getCamera().viewToLocal(hb);

    PNodes.setPosition(attrLabelsNode, cb.getX(),
        cb.getY() + Math.max(0, hb.getMinY() - cb.getMinY() - attrLabelsNode.getHeight()));
    PNodes.setPosition(originLabelsNode,
        cb.getMinX() + Math.max(0, hb.getMinX() - cb.getMinX() - originLabelsNode.getWidth()),
        cb.getY());

    PNodes.setPosition(destLabelsNode,
        cb.getMaxX() - Math.max(destLabelsNode.getWidth(), cb.getMaxX() - hb.getMaxX()),
        cb.getY());
  }

  @Override
  public void fitInView() {
    PCamera camera = getCamera();
    PBounds b = heatmapNode.getFullBounds();  // to be adjusted

    // margins to ensure there is enough space for the floating labels
    Insets m = new Insets(
        (int)attrLabelsNode.getHeight(), (int)originLabelsNode.getWidth(),
        0, (int)destLabelsNode.getWidth());


    if (b.height > b.width * 10) {  // if the height of the heatmap is much larger than width,
                                    // show only a part of the heatmap
      PBounds cb = camera.getBounds();
      cb.setRect(                 // subtract margins to have the proper aspect ratio
          cb.x + m.left, cb.y + m.top,
          cb.width - m.left - m.right, cb.height - m.top - m.bottom);
      camera.localToView(cb);
      b.height = b.width * (cb.height / cb.width) * 1.2;
    }

    PiccoloUtils.animateViewToPaddedBounds(camera, b, m, 0);
  }

  @Override
  public Rectangle2D getEdgeLabelBounds(Edge edge, FlowEndpoint ep) {
    return null;
  }

  @Override
  public Point2D getFlowLineInPoint(int row, FlowEndpoint ep) {
    return null;
  }

  private LabelIterator createAttrsLabelIterator() {
    return new LabelIterator() {

      Iterator<String> it = null;
      int attrIndex = 0;
      double pos;

      public double getPosition() {
        return pos;
      }

      public double getSize() {
        return heatmapNode.getCellWidth();
      }

      public boolean hasNext() {
        return it.hasNext();
      }

      public String next() {
        String label = it.next();
        pos =
          heatmapNode.getBoundsReference().getX() +
          attrIndex * (heatmapNode.getCellWidth() + heatmapNode.getCellSpacing());

        attrIndex++;
        return label;
      }

      public void reset() {
        pos = Double.NaN;
        attrIndex = 0;
        it = attrs().iterator();
      }

      private List<String> attrs() {
        return getFlowstratesView().getFlowMapGraph().getEdgeWeightAttrs();
      }
    };
  }

  private LabelIterator createNodeLabelIterator(final FlowEndpoint ep) {
    return new LabelIterator() {

      Iterator<String> it = null;
      int index = 0;
      double pos;

      public double getPosition() {
        return pos;
      }

      public double getSize() {
        return heatmapNode.getCellHeight();
      }

      public boolean hasNext() {
        return it.hasNext();
      }

      public String next() {
        String label = it.next();
        pos =
          heatmapNode.getBoundsReference().getY() +
          index * (heatmapNode.getCellHeight() + heatmapNode.getCellSpacing());

        index++;
        return label;
      }

      public void reset() {
        pos = Double.NaN;
        index = 0;
        List<Edge> edges = getFlowstratesView().getVisibleEdges();
        final FlowMapGraph fmg = getFlowstratesView().getFlowMapGraph();
        it = Iterators.transform(edges.iterator(), new Function<Edge, String>() {
          @Override
          public String apply(Edge e) {
            Node node = ep.nodeOf(e);
            String name = fmg.getNodeLabel(node);
            if (name.length() > MAX_NODE_NAME_LENGTH) {
              name = name.substring(0, MAX_NODE_NAME_LENGTH - 2) + ".";
            }
            return name;
          }
        });
      }
    };
  }




}