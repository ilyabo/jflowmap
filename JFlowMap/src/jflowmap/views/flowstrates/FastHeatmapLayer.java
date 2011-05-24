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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.List;

import javax.swing.SwingUtilities;

import jflowmap.FlowEndpoint;
import jflowmap.FlowMapGraph;
import jflowmap.util.Pair;
import jflowmap.util.piccolo.PNodes;
import jflowmap.util.piccolo.PiccoloUtils;
import prefuse.data.Edge;
import at.fhjoanneum.cgvis.data.IColorForValue;
import at.fhjoanneum.cgvis.data.IDataValues;
import at.fhjoanneum.cgvis.plots.FloatingLabelsNode;
import at.fhjoanneum.cgvis.plots.FloatingLabelsNode.LabelIterator;
import at.fhjoanneum.cgvis.plots.mosaic.MosaicPlotNode;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PDimension;

/**
 * @author Ilya Boyandin
 */
public class FastHeatmapLayer extends TemporalViewLayer {

  private static final Font NODE_LABELS_FONT = new Font("Arial", Font.PLAIN, 10);
  private static final Color FLOATING_LABELS_BG = new Color(255, 255, 255, 220);
  private static final int MAX_NODE_NAME_LENGTH = 18;
  private MosaicPlotNode heatmapNode;
  private final IColorForValue colorForValue;
  private final FloatingLabelsNode attrLabelsNode;
  private final FloatingLabelsNode originLabelsNode;

  private final FloatingLabelsNode destLabelsNode;
  private Pair<List<String>,List<String>> nodeLabels;
  private final FontMetrics nodeLabelsFontMetrics;

  public FastHeatmapLayer(FlowstratesView flowstratesView) {
    super(flowstratesView);
    colorForValue = createColorForValue();
    nodeLabelsFontMetrics = getFlowstratesView().getVisualCanvas().getFontMetrics(NODE_LABELS_FONT);

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
        adjustFloatingLabelNodePositions();
      }
    });

    FastHeatmapCursor cursor = new FastHeatmapCursor(this);
    getCamera().addChild(cursor);
    cursor.moveToBack();

  }

  public MosaicPlotNode getHeatmapNode() {
    return heatmapNode;
  }

  private FloatingLabelsNode createFloatingLabels(
      boolean isHorizontal, LabelIterator it, boolean anchorLabelsToEnd) {
    FloatingLabelsNode labels = new FloatingLabelsNode(isHorizontal, it);
    labels.setFont(NODE_LABELS_FONT);
    labels.setAnchorLabelsToEnd(anchorLabelsToEnd);
    labels.setMarginBefore(anchorLabelsToEnd ? 0 : 3);
    labels.setMarginAfter(anchorLabelsToEnd ? 3 : 0);
    labels.setPaint(FLOATING_LABELS_BG);
//    labels.setPickable(false);
    getCamera().addChild(labels);
    return labels;
  }

  @Override
  public void renew() {
    removeAllChildren();
//    getCamera().removeAllChildren();

    nodeLabels = null;

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

  private void adjustFloatingLabelNodePositions() {
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
  public Dimension2D getEdgeLabelBounds(Edge edge, FlowEndpoint ep) {
//    int index = getFlowstratesView().getVisibleEdgeIndex(edge);
    String label = getFlowMapGraph().getNodeLabel(ep.nodeOf(edge));
    PDimension d = new PDimension(
        SwingUtilities.computeStringWidth(nodeLabelsFontMetrics, label) +
        (ep == FlowEndpoint.ORIGIN ? 7 : 2),
        nodeLabelsFontMetrics.getAscent());
    getCamera().localToView(d);
    return d;
  }

  @Override
  public Point2D getFlowLineInPoint(int row, FlowEndpoint ep) {
    switch (ep) {

    case ORIGIN:
      PBounds ob = originLabelsNode.getBounds();
      ob.x -= 3;
      getCamera().localToView(ob);
      return new Point2D.Double(ob.getMaxX(), calcNodeLabelYPos(row) + heatmapNode.getCellHeight()/2.0);

    case DEST:
      PBounds db = destLabelsNode.getBounds();
      db.x += 3;
      getCamera().localToView(db);
      return new Point2D.Double(db.getMinX(), calcNodeLabelYPos(row) + heatmapNode.getCellHeight()/2.0);

    default:
      throw new AssertionError();
    }
  }

  double getTupleY(int row) {
    return row * heatmapNode.getCellHeight();
  }

  private List<String> getNodeLabels(FlowEndpoint ep) {
    if (nodeLabels == null) {
      nodeLabels = Pair.of(nodeLabels(FlowEndpoint.ORIGIN), nodeLabels(FlowEndpoint.DEST));
    }

    switch (ep) {
    case ORIGIN: return nodeLabels.first();
    case DEST: return nodeLabels.second();
    default: throw new AssertionError();
    }
  }

  private List<String> nodeLabels(final FlowEndpoint ep) {
    List<Edge> edges = getFlowstratesView().getVisibleEdges();
    final FlowMapGraph fmg = getFlowstratesView().getFlowMapGraph();

    return Lists.transform(edges, new Function<Edge, String>() {
      @Override public String apply(Edge e) { return /*shorten*/(fmg.getNodeLabel(ep.nodeOf(e))); }
    });
  }

  private double calcNodeLabelYPos(int index) {
    return
      heatmapNode.getBoundsReference().getY() +
      index * (heatmapNode.getCellHeight() + heatmapNode.getCellSpacing());
  }

  private static String shorten(String name) {
    if (name.length() > MAX_NODE_NAME_LENGTH) {
      name = name.substring(0, MAX_NODE_NAME_LENGTH - 2) + ".";
    }
    return name;
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
        pos = calcNodeLabelYPos(index);

        index++;
        return label;
      }

      public void reset() {
        pos = Double.NaN;
        index = 0;
        it = getNodeLabels(ep).iterator();
      }

    };
  }

}