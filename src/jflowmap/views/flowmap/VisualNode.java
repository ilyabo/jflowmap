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

import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jflowmap.geom.GeomUtils;
import jflowmap.geom.Point;
import jflowmap.util.piccolo.PNodes;
import jflowmap.views.ColorCodes;

import org.apache.log4j.Logger;

import prefuse.data.Node;

import com.google.common.base.Function;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventListener;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolox.util.PFixedWidthStroke;

/**
 * @author Ilya Boyandin
 */
public class VisualNode extends PNode {

  private static final long serialVersionUID = 1L;
  private static Logger logger = Logger.getLogger(VisualNode.class);
  public enum Attributes {
    SELECTED, HIGHLIGHTED
  }

  private static final Stroke STROKE = null;
  private static final Stroke HIGHLIGHTED_STROKE = new PFixedWidthStroke(1);
  private static final Stroke SELECTED_STROKE = new PFixedWidthStroke(2);

  private final List<VisualEdge> outgoingEdges = new ArrayList<VisualEdge>();
  private final List<VisualEdge> incomingEdges = new ArrayList<VisualEdge>();

  private final VisualFlowMap visualFlowMap;

  private final Node node;

  private final double origX;
  private final double origY;


  private PPath marker;

//  private static final Font LABEL_FONT = new Font("Dialog", Font.BOLD, 5);

  public VisualNode(VisualFlowMap visualFlowMap, Node node, double origX, double origY) {
    if (Double.isNaN(origX)  ||  Double.isNaN(origY)) {
      logger.error("NaN coordinates passed in for node: " + node);
      throw new IllegalArgumentException("NaN coordinates passed in for node " + node);
    }
    this.origX = origX;
    this.origY = origY;
    this.node = node;
    this.visualFlowMap = visualFlowMap;
    addInputEventListener(INPUT_EVENT_HANDLER);

//    PText label = new PText(visualFlowMap.getLabel(node));
//    label.setFont(LABEL_FONT);
//    label.setTextPaint(Color.white);
//    label.setX(x);
//    label.setY(y + 5);
//    addChild(label);
//    label.moveToBack();

    updateSize();
    updateVisibility();
  }

  public Point2D getPoint() {
    return new Point2D.Double(origX, origY);
  }

  private Shape createNodeShape(double x, double y) {
    double size = getSize();
    return new Ellipse2D.Double(x - size/2, y - size/2, size, size);
  }

  private double getSize() {
    return visualFlowMap.getModel().getNodeSize();
  }

  protected void updateVisibility() {
    boolean visibility = (visualFlowMap.getModel().getShowNodes()  ||  isHighlighted()  ||  isSelected());
    marker.setVisible(visibility);
  }

  protected void updatePositionInCamera(PCamera cam) {
    Point2D p = getPoint();
    double size = getSize();
    setVisible(cam.getViewBounds().contains(p));
//      labelNode.setVisible(cam.getBounds().contains(labelNode.getFullBounds()));
    cam.viewToLocal(p);
    p.setLocation(p.getX(), p.getY());
    PNodes.setPosition(this, p.getX(), p.getY(), true);
  }

  protected void updateSize() {
    if (marker != null) {
      removeChild(marker);
    }
    marker = new PPath(createNodeShape(0, 0));
    marker.setPaint(visualFlowMap.getColor(ColorCodes.NODE_PAINT));
    marker.setStroke(null);
    addChild(marker);
    marker.moveToBack();
  }

  public double getValueX() {
  	return origX;
  }

  public double getValueY() {
    return origY;
  }

//  public void createNodeShape() {
//    double sum = 0;
//    for (VisualEdge vedge : outgoingEdges) {
//    	sum += vedge.getEdge().getDouble("value");
//    }
//    for (VisualEdge vedge : incomingEdges) {
//			sum += vedge.getEdge().getDouble("value");
//		}
//
//    double r = Math.sqrt(sum)/50;
//    PPath ppath = new PPath(new Ellipse2D.Double(x - r/2, y - r/2, r, r));
//    ppath.setStrokePaint(STROKE_PAINT);
//    ppath.setPaint(PAINT);
//    ppath.setStroke(STROKE);
//
//    addChild(ppath);
//  }


  public Node getNode() {
    return node;
  }

  public VisualFlowMap getVisualGraph() {
    return visualFlowMap;
  }

  public String getLabel() {
    StringBuilder sb = new StringBuilder();
    String labelAttr = visualFlowMap.getLabelAttr();
    if (labelAttr != null) {
      sb.append(node.getString(labelAttr));
    } else {
      sb.append("(").append(getValueX()).append(",").append(getValueY()).append(")");
    }
    return sb.toString();
  }

  private static final PInputEventListener INPUT_EVENT_HANDLER = new PBasicInputEventHandler() {
    @Override
    public void mouseClicked(PInputEvent event) {
      VisualNode vnode = PNodes.getAncestorOfType(event.getPickedNode(), VisualNode.class);
      vnode.visualFlowMap.setSelectedNode(vnode.isSelected() ? null : vnode);
    }

    @Override
    public void mouseEntered(PInputEvent event) {
      VisualNode vnode = PNodes.getAncestorOfType(event.getPickedNode(), VisualNode.class);
      vnode.setHighlighted(true);
      vnode.getVisualGraph().showTooltip(vnode, event.getPosition());
    }

    @Override
    public void mouseExited(PInputEvent event) {
      VisualNode vnode = PNodes.getAncestorOfType(event.getPickedNode(), VisualNode.class);
      vnode.setHighlighted(false);
      vnode.getVisualGraph().hideTooltip();
    }
  };

  public void addOutgoingEdge(VisualEdge flow) {
    outgoingEdges.add(flow);
  }

  public List<VisualEdge> getOutgoingEdges() {
    return Collections.unmodifiableList(outgoingEdges);
  }

  public void addIncomingEdge(VisualEdge flow) {
    incomingEdges.add(flow);
  }

  public List<VisualEdge> getIncomingEdges() {
    return Collections.unmodifiableList(incomingEdges);
  }

  /**
   * Returns a newly created and modifiable list of
   * incoming and outgoing edges of the node.
   */
  public List<VisualEdge> getEdges() {
    List<VisualEdge> edges = new ArrayList<VisualEdge>(incomingEdges.size() + outgoingEdges.size());
    edges.addAll(outgoingEdges);
    edges.addAll(incomingEdges);
    return edges;
  }

  /**
   * Returns an unmodifiable list of incoming edges if incoming is true
   * and outgoing if incoming is false.
   */
  public List<VisualEdge> getEdges(boolean incoming) {
    if (incoming) {
      return getIncomingEdges();
    } else {
      return getOutgoingEdges();
    }
  }

  /**
   * Throws an exception if there is more than one such node.
   */
  public VisualEdge getEdgeByOppositeNode(VisualNode n, boolean incoming) {
    List<VisualEdge> edges = getEdges(incoming);
    VisualEdge found = null;
    for (VisualEdge ve : edges) {
      if (ve.getOppositeNode(this) == n) {
        if (found != null) {
          throw new IllegalStateException("There are more than one edges between the nodes");
        }
        found = ve;
      }
    }
    return found;
  }

  public List<VisualNode> getEdgeOppositeNodes(boolean incoming) {
    List<VisualEdge> edges = getEdges(incoming);
    List<VisualNode> nodes = new ArrayList<VisualNode>(edges.size());
    for (VisualEdge ve : edges) {
      nodes.add(ve.getOppositeNode(this));
    }
    return nodes;
  }

  public boolean isSelected() {
    return getBooleanAttribute(Attributes.SELECTED.name(), false);
  }

  public void setSelected(boolean selected) {
    addAttribute(Attributes.SELECTED.name(), selected);
    updateVisibility();
    updateColorsAndStroke();
    updateEdgeColors();
  }

  public boolean isHighlighted() {
    return getBooleanAttribute(Attributes.HIGHLIGHTED.name(), false);
  }

  public void setHighlighted(boolean highlighted) {
    addAttribute(Attributes.HIGHLIGHTED.name(), highlighted);
    updateVisibility();
    updateColorsAndStroke();
    updateEdgeColors();
  }

  void updateColorsAndStroke() {
    boolean selected = isSelected();
    boolean highlighted = isHighlighted();
    if (selected) {
      marker.setStroke(SELECTED_STROKE);
      marker.setStrokePaint(visualFlowMap.getColor(ColorCodes.NODE_SELECTED_STROKE_PAINT));
      marker.setPaint(visualFlowMap.getColor(ColorCodes.NODE_SELECTED_PAINT));
    } else if (highlighted) {
      marker.setStroke(HIGHLIGHTED_STROKE);
      marker.setStrokePaint(visualFlowMap.getColor(ColorCodes.NODE_STROKE_PAINT));
      marker.setPaint(visualFlowMap.getColor(ColorCodes.NODE_HIGHLIGHTED_PAINT));
    } else {
      marker.setStroke(STROKE);
      marker.setStrokePaint(visualFlowMap.getColor(ColorCodes.NODE_STROKE_PAINT));
      marker.setPaint(visualFlowMap.getColor(ColorCodes.NODE_PAINT));
    }
  }

  private void updateEdgeColors() {
    boolean highlightEdges = (isSelected() || isHighlighted());
    for (VisualEdge flow : outgoingEdges) {
      if (flow.getVisible()) {
        flow.setHighlighted(highlightEdges, true, false);
      }
    }
    for (VisualEdge flow : incomingEdges) {
      if (flow.getVisible()) {
        flow.setHighlighted(highlightEdges, true, true);
      }
    }
  }

  @Override
  public String toString() {
    return "VisualNode{" +
        "label=" + getLabel() +
        '}';
  }

  protected void updatePickability() {
//    boolean pickable = false;
//    for (VisualEdge ve : outgoingEdges) {
//      if (ve.getVisible()) {
//        pickable = true;
//        break;
//      }
//    }
//    if (!pickable)
//    for (VisualEdge ve : incomingEdges) {
//      if (ve.getVisible()) {
//        pickable = true;
//        break;
//      }
//    }
//    setPickable(pickable);
//    setChildrenPickable(pickable);
  }


  /**
   * Returns a list of the opposite nodes of the node's incoming/outgoing edges.
   * @param ofIncomingEdges False for outgoing edges
   * @return Opposite nodes of incoming edges if ofIncomingEdges is true or of
   *     outgoing edges if ofIncomingEdges is false.
   */
  public List<VisualNode> getOppositeNodes(boolean ofIncomingEdges) {
    List<VisualEdge> edges;
    if (ofIncomingEdges) {
      edges = incomingEdges;
    } else {
      edges = outgoingEdges;
    }
    List<VisualNode> nodes = new ArrayList<VisualNode>(edges.size());
    for (VisualEdge edge : edges) {
      VisualNode opposite;
      if (ofIncomingEdges) {
        opposite = edge.getSourceNode();
      } else {
        opposite = edge.getTargetNode();
      }
      nodes.add(opposite);
    }
    return nodes;
   }

   public double distanceTo(VisualNode node) {
     return GeomUtils.distance(getValueX(), getValueY(), node.getValueX(), node.getValueY());
   }

   public boolean hasIncomingEdges() {
     return incomingEdges.size() > 0;
   }

  public boolean hasOutgoingEdges() {
    return outgoingEdges.size() > 0;
  }

  public boolean hasEdges() {
    return hasIncomingEdges()  ||  hasOutgoingEdges();
  }

  public static final Comparator<VisualNode> LABEL_COMPARATOR = new Comparator<VisualNode>() {
    public int compare(VisualNode o1, VisualNode o2) {
      return o1.getLabel().compareTo(o2.getLabel());
    }
  };

  public static final Function<VisualNode, Point> TRANSFORM_NODE_TO_POSITION = new Function<VisualNode, Point>() {
    public Point apply(VisualNode node) {
      return new Point(node.getX(), node.getY());
    }
  };

}
