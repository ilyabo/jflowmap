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
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import jflowmap.geom.GeomUtils;
import jflowmap.util.Pair;
import jflowmap.util.piccolo.PNodes;
import jflowmap.views.ColorCodes;

import org.apache.log4j.Logger;

import prefuse.data.Edge;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventListener;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * @author Ilya Boyandin
 */
public abstract class VisualEdge extends PNode {

  private static Logger logger = Logger.getLogger(VisualEdge.class);

  protected static final Object ATTR_ANIMATION_ABS_EDGE_WEIGHT = "animAbsWeight";
  private static final int MAX_EDGE_WIDTH = 100;
  private static final long serialVersionUID = 1L;

  private final VisualFlowMap visualFlowMap;
  private final VisualNode sourceNode;
  private final VisualNode targetNode;
  private final Edge edge;
  private final boolean isSelfLoop;

  private final double edgeLength;

  private PPath edgePPath;

  private boolean highlighted;

  public VisualEdge(VisualFlowMap visualFlowMap, Edge edge, VisualNode sourceNode, VisualNode targetNode) {
    this.edge = edge;
    this.sourceNode = sourceNode;
    this.targetNode = targetNode;
    this.visualFlowMap = visualFlowMap;
    this.isSelfLoop = visualFlowMap.getFlowMapGraph().isSelfLoop(edge);
    if (isSelfLoop) {
      this.edgeLength = 0;
    } else {
      this.edgeLength = getDistance(sourceNode, targetNode);
    }
  }

  /** Must be called by subclasses */
  protected void init() {
    PPath ppath = createEdgePPath();
    setEdgePPath(ppath);
    addChild(ppath);
    updateEdgeWidth();
    addInputEventListener(visualEdgeListener);
  }

  protected abstract PPath createEdgePPath();

  public boolean isSelfLoop() {
    return isSelfLoop;
  }

  protected Shape createSelfLoopShape() {
    Shape shape;

    Rectangle2D b = getSelfLoopBounds();
    shape = new Ellipse2D.Double(b.getX(), b.getY(), b.getWidth(), b.getHeight());

////    final double size = SELF_LOOP_CIRCLE_SIZE;
////    MinMax xstats = visualFlowMap.getGraphStats().getNodeXStats();
////    MinMax ystats = visualFlowMap.getGraphStats().getNodeYStats();
//    final double size = visualFlowMap.getStats().getEdgeLengthStats().getAvg() / 15;
////
////    final double xsize = (xstats.getMax() - xstats.getMin()) / 20;
////    final double ysize = (ystats.getMax() - ystats.getMin()) / 20;
//    shape = new BSplinePath(Arrays.asList(new Point[] {
//        new Point(x1, y1),
//        new Point(x1 - size/2, y1 + size/2),
//        new Point(x1, y1 + size),
//        new Point(x1 + size/2, y1 + size/2),
//        new Point(x1, y1)
//    }));
    return shape;
  }

  public double getSourceX() {
    return sourceNode.getValueX();
  }

  public double getSourceY() {
    return sourceNode.getValueY();
  }

  public double getTargetX() {
    return targetNode.getValueX();
  }

  public double getTargetY() {
    return targetNode.getValueY();
  }

  protected void setEdgePPath(PPath ppath) {
    this.edgePPath = ppath;
  }

  protected PPath getEdgePPath() {
    return edgePPath;
  }

  public void updateEdgeWidth() {
    updateEdgeWidthTo(getEdgeWeight());
  }

  public void updateEdgeWidthTo(double value) {
    PPath ppath = getEdgePPath();
    if (ppath != null) {
      if (isSelfLoop) {
//        ppath.setBounds(getSelfLoopBoundsFor(value));
        ppath.setPathTo(createSelfLoopShape());
        ppath.setStroke(null);
      } else {
        double strokeWidth = normalizeForWidthScale(value);
        Stroke stroke = createStrokeFor(strokeWidth);
        ppath.setStroke(stroke);
      }
    }
    updateVisibilityFor(value);
  }

  public void updateEdgeColors() {
    updateEdgeColorsTo(getEdgeWeight());
  }

  public void updateEdgeColorsTo(double value) {
    PPath ppath = getEdgePPath();
    if (ppath != null) {
      double normValue = normalizeForColorScale(value);
      if (isSelfLoop) {
        ppath.setPaint(createPaintFor(normValue));
      } else {
        ppath.setStrokePaint(createPaintFor(normValue));
      }
    }
  }

  private Rectangle2D.Double getSelfLoopBounds() {
    return getSelfLoopBoundsFor(getEdgeWeight());
  }

  private Rectangle2D.Double getSelfLoopBoundsFor(double value) {
    double size = getSelfLoopSizeFor(value);
    return new Rectangle2D.Double(getSourceX() - size/2, getSourceY() - size/2, size, size);
  }

  private double getSelfLoopSizeFor(double value) {
    double linewidth = Math.max(1, visualFlowMap.getModel().getMaxEdgeWidth());
    double normAbsValue = Math.abs(normalizeForWidthScale(value));
    if (Double.isNaN(normAbsValue)) {
      return 0;
    }
    double avgLen = visualFlowMap.getStats().getEdgeLengthStats().getAvg();
    return avgLen / MAX_EDGE_WIDTH * linewidth * normAbsValue;
  }

  public void updateVisibility() {
    updateVisibilityFor(getEdgeWeight());
  }

  public void updateVisibilityFor(double value) {
    boolean visible = getVisibilityFor(value);

    setVisible(visible);
    setPickable(visible);
    setChildrenPickable(visible);
  }

  public boolean getVisibilityFor(double value) {
    final VisualFlowMapModel model = visualFlowMap.getModel();
    double weightFilterMin = model.getEdgeWeightFilterMin();
    double weightFilterMax = model.getEdgeWeightFilterMax();

//    double edgeLengthFilterMin = model.getEdgeLengthFilterMin();
//    double edgeLengthFilterMax = model.getEdgeLengthFilterMax();
//    double length = getEdgeLength();

    double absValue = Math.abs(value);

    PBounds vb = getVisualFlowMap().getCamera().getViewBounds();

    boolean visible =
        !Double.isNaN(value)  &&
        value != 0.0  &&
//        model.getEdgeFilter().accepts(this)
        weightFilterMin <= absValue && absValue <= weightFilterMax  &&
//        edgeLengthFilterMin <= length && length <= edgeLengthFilterMax &&
        (!isSelfLoop()  ||  visualFlowMap.getModel().getShowSelfLoops())  &&
        (vb.contains(getSourceNode().getPoint())  ||  vb.contains(getTargetNode().getPoint()))
    ;

    return visible;
  }


  public Edge getEdge() {
    return edge;
  }

  public VisualFlowMap getVisualFlowMap() {
    return visualFlowMap;
  }

  public String getLabel() {
    return sourceNode.getLabel() + "  -->  " +
         targetNode.getLabel();
  }

  public double getEdgeWeight() {
    return edge.getDouble(visualFlowMap.getValueAttr());
  }

  public double getEdgeLength() {
    return edgeLength;
  }

  public VisualNode getSourceNode() {
    return sourceNode;
  }

  public VisualNode getTargetNode() {
    return targetNode;
  }

  /**
   * Returns source node if source is true or target node otherwise.
   */
  public VisualNode getNode(boolean source) {
    if (source) {
      return getSourceNode();
    } else {
      return getTargetNode();
    }
  }

  public VisualNode getOppositeNode(VisualNode node) {
    if (targetNode == node) {
      return sourceNode;
    }
    if (sourceNode == node) {
      return targetNode;
    }
    throw new IllegalArgumentException(
        "Node '" + node.getLabel() + "' is neither the source nor the target node of the edge '" + getLabel() + "'");
  }

  @Override
  public String toString() {
    return "VisualEdge{" +
        "label='" + getLabel() + "', " +
        "value=" + getEdgeWeight() +
    '}';
  }

  private double normalizeForWidthScale(double value) {
    return visualFlowMap.getModel().normalizeForWidthScale(value);
  }

  private double normalizeForColorScale(double value) {
    return visualFlowMap.getModel().normalizeForColorScale(value);
  }

  private Paint createPaintFor(double normValue) {
    return visualFlowMap.getVisualEdgePaintFactory().createPaint(
        normValue, getSourceX(), getSourceY(), getTargetX(), getTargetY(),
        edgeLength, isSelfLoop);
  }

  protected Stroke createStrokeFor(double normValue) {
    return visualFlowMap.getVisualEdgeStrokeFactory().createStroke(normValue);
  }

  public void setHighlighted(boolean value, boolean showDirection, boolean asOutgoing) {
    setHighlighted(value, showDirection, asOutgoing, true);
  }

  public void setHighlighted(boolean value, boolean showDirection, boolean asOutgoing,
      boolean propagateEvent) {
    if (this.highlighted != value) {
      this.highlighted = value;
      PPath ppath = getEdgePPath();
      if (ppath != null) {
        Paint paint;
        if (logger.isDebugEnabled()) {
          logger.debug((value ? "H" : "Unh") + "ighlight edge [" + getLabel() + " (" +
              visualFlowMap.getValueAttr() + " = " + getEdgeWeight() + ")]");
        }
        if (value) {
          Color color;
          if (showDirection) {
            if (asOutgoing) {
              color = visualFlowMap.getColor(ColorCodes.EDGE_STROKE_HIGHLIGHTED_OUTGOING_PAINT);
            } else {
              color = visualFlowMap.getColor(ColorCodes.EDGE_STROKE_HIGHLIGHTED_INCOMING_PAINT);
            }
          } else {
            color = visualFlowMap.getColor(ColorCodes.EDGE_STROKE_HIGHLIGHTED_PAINT);
          }
          paint = color;
        } else {
          paint = createPaintFor(normalizeForColorScale(getEdgeWeight()));
        }
        ppath.setStrokePaint(paint);
      }
      repaint();
      if (propagateEvent) {
        getVisualFlowMap().firePropertyChange(
            VisualFlowMap.PROPERTY_CODE_HIGHLIGHTED, VisualFlowMap.PROPERTY_HIGHLIGHTED,
            Pair.of(edge, !value), Pair.of(edge, value));
      }
    }
  }

  public void update() {
//    updateVisibility();
    updateEdgeColors();
    updateEdgeWidth();
  }

  private static final PInputEventListener visualEdgeListener = new PBasicInputEventHandler() {
    @Override
    public void mouseEntered(PInputEvent event) {
      VisualEdge ve = PNodes.getAncestorOfType(event.getPickedNode(), VisualEdge.class);
      if (ve != null) {
        ve.setHighlighted(true, false, false);
        ve.getVisualFlowMap().showTooltip(ve, event.getPosition());
      }
//      node.moveToFront();
    }

    @Override
    public void mouseExited(PInputEvent event) {
      VisualEdge ve = PNodes.getAncestorOfType(event.getPickedNode(), VisualEdge.class);
      if (ve != null) {
//        if (!ve.getVisible()) {
//          return;
//        }
        ve.setHighlighted(false, false, false);
        ve.getVisualFlowMap().hideTooltip();
      }
    }
  };

  private double getDistance(VisualNode n1, VisualNode n2) {
    final double x1 = n1.getValueX();
    final double y1 = n1.getValueY();
    final double x2 = n2.getValueX();
    final double y2 = n2.getValueY();
    double length = GeomUtils.distance(x1, y1, x2, y2);
    return length;
  }

}
