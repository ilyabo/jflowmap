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

package jflowmap.visuals;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.util.Arrays;

import jflowmap.geom.BSplinePath;
import jflowmap.geom.GeomUtils;
import jflowmap.geom.Point;
import jflowmap.util.piccolo.PNodes;

import org.apache.log4j.Logger;

import prefuse.data.Edge;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventListener;
import edu.umd.cs.piccolo.nodes.PPath;

/**
 * @author Ilya Boyandin
 */
public abstract class VisualEdge extends PNode {

  private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(VisualEdge.class);

  private final VisualFlowMap visualFlowMap;
  private final VisualNode sourceNode;
  private final VisualNode targetNode;
  private final Edge edge;
  private final boolean isSelfLoop;

  private final double edgeLength;

  private PPath edgePPath;

  public VisualEdge(VisualFlowMap visualFlowMap, Edge edge, VisualNode sourceNode, VisualNode targetNode) {
    this.edge = edge;
    this.sourceNode = sourceNode;
    this.targetNode = targetNode;
    this.visualFlowMap = visualFlowMap;
    this.isSelfLoop = visualFlowMap.getFlowMapGraph().isSelfLoop(edge);
    if (isSelfLoop) {
      this.edgeLength = 0;
    } else {
      final double x1 = sourceNode.getValueX();
      final double y1 = sourceNode.getValueY();
      final double x2 = targetNode.getValueX();
      final double y2 = targetNode.getValueY();
      this.edgeLength = GeomUtils.distance(x1, y1, x2, y2);
    }

    addInputEventListener(visualEdgeListener);
  }

  public boolean isSelfLoop() {
    return isSelfLoop;
  }

  protected Shape createSelfLoopShape() {
    Shape shape;
    final double x1 = sourceNode.getValueX();
    final double y1 = sourceNode.getValueY();

//    shape = new Ellipse2D.Double(
//        x1 - SELF_LOOP_CIRCLE_SIZE/2, y1,
//        SELF_LOOP_CIRCLE_SIZE, SELF_LOOP_CIRCLE_SIZE);

//    final double size = SELF_LOOP_CIRCLE_SIZE;
    final double size = visualFlowMap.getStats().getEdgeLengthStats().getAvg() / 8;
//    MinMax xstats = visualFlowMap.getGraphStats().getNodeXStats();
//    MinMax ystats = visualFlowMap.getGraphStats().getNodeYStats();
//
//    final double xsize = (xstats.getMax() - xstats.getMin()) / 20;
//    final double ysize = (ystats.getMax() - ystats.getMin()) / 20;
    shape = new BSplinePath(Arrays.asList(new Point[] {
        new Point(x1, y1),
        new Point(x1 - size/2, y1 + size/2),
        new Point(x1, y1 + size),
        new Point(x1 + size/2, y1 + size/2),
        new Point(x1, y1)
    }));
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
    PPath ppath = getEdgePPath();
    if (ppath != null) {
      ppath.setStroke(createStroke());
    }
  }

//  public abstract void updateEdgeMarkerColors();

  public void updateVisibility() {
    final VisualFlowMapModel model = visualFlowMap.getModel();
    double weightFilterMin = model.getEdgeWeightFilterMin();
    double weightFilterMax = model.getEdgeWeightFilterMax();

    double edgeLengthFilterMin = model.getEdgeLengthFilterMin();
    double edgeLengthFilterMax = model.getEdgeLengthFilterMax();
    final double weight = getEdgeWeight();
    double length = getEdgeLength();

    boolean visible =
        weightFilterMin <= weight && weight <= weightFilterMax  &&
        edgeLengthFilterMin <= length && length <= edgeLengthFilterMax
    ;

    if (visible) {
      if (visualFlowMap.hasClusters()) {
        VisualNodeCluster srcCluster = visualFlowMap.getNodeCluster(getSourceNode());
        VisualNodeCluster targetCluster = visualFlowMap.getNodeCluster(getTargetNode());

        // TODO: why do we need these null checks here? i.e. why some countries don't have a cluster
        visible = (srcCluster != null  &&  srcCluster.getTag().isVisible())  ||
              (targetCluster != null  &&  targetCluster.getTag().isVisible());
      }
    }
    setVisible(visible);
    setPickable(visible);
    setChildrenPickable(visible);
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
    return edge.getDouble(visualFlowMap.getFlowMapGraph().getEdgeWeightAttr());
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

  public void updateEdgeColors() {
    PPath ppath = getEdgePPath();
    if (ppath != null) {
      ppath.setStrokePaint(createPaint());
    }
  }

  private Paint createPaint() {
    double normalizedValue = visualFlowMap.getModel().normalizeEdgeWeightForColorScale(getEdgeWeight());
    return visualFlowMap.getVisualEdgePaintFactory().createPaint(
        normalizedValue, getSourceX(), getSourceY(), getTargetX(), getTargetY(),
        edgeLength, isSelfLoop);
  }

  protected Stroke createStroke() {
    double normalizedValue = visualFlowMap.getModel().normalizeEdgeWeightForWidthScale(getEdgeWeight());
    return visualFlowMap.getVisualEdgeStrokeFactory().createStroke(normalizedValue);
  }

  public void setHighlighted(boolean value, boolean showDirection, boolean asOutgoing) {
    PPath ppath = getEdgePPath();
    if (ppath != null) {
      Paint paint;
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
//        paint = getValueColor(color, false);
        paint = color;
      } else {
        paint = createPaint();
      }
      ppath.setStrokePaint(paint);
//      getSourceNode().setVisible(value);
//      getTargetNode().setVisible(value);
    }
  }

  public void update() {
    updateEdgeColors();
//    updateEdgeMarkerColors();
    updateEdgeWidth();
    updateVisibility();
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
        if (!ve.getVisible()) {
          return;
        }
        ve.setHighlighted(false, false, false);
        ve.getVisualFlowMap().hideTooltip();
      }
    }
  };

}
