package jflowmap.views.flowstrates;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import jflowmap.geom.GeomUtils;
import jflowmap.util.piccolo.PNodes;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * @author Ilya Boyandin
 */
class Centroid extends PPath {
  private final static Font LABEL_FONT = new Font("Arial", Font.PLAIN, 10/*9*/);
  private final Point2D point;
  private final double size;
  private final PText labelText;
  private boolean isSelected;
  private boolean isHighlighted;
  private final FlowstratesView view;
  private final String nodeId;
  private final double origX;
  private final double origY;
  private CentroidTimeSlider timeSlider;

  public Centroid(String nodeId, String nodeLabel, double origX, double origY,
      double size, Paint paint, FlowstratesView view) {
    super(new Ellipse2D.Double(origX, origY, size, size));
    this.origX = origX;
    this.origY = origY;
    this.view = view;
    this.point = new Point2D.Double(origX, origY);
    this.size = size;
    this.nodeId = nodeId;

    this.labelText = new PText(FlowstratesView.shortenNodeLabel(nodeLabel));
    labelText.setFont(LABEL_FONT);
    addChild(labelText);

    setStroke(null);
    updateColors();
  }

  public Rectangle2D getCollisionBounds() {
    PBounds b = getLabelNode().getBounds();
    GeomUtils.growRectInPlaceByRelativeSize(b, .4, .1, .1, .1);
    return b;
  }

  public void setOpaque(boolean opaque) {
    setTransparency(opaque ? 1.0f : 0.15f);
  }

  public FlowstratesView getView() {
    return view;
  }

  public String getNodeId() {
    return nodeId;
  }

  public double getOrigX() {
    return origX;
  }

  public double getOrigY() {
    return origY;
  }

  public boolean isSelected() {
    return isSelected;
  }

  public void setSelected(boolean selected) {
    if (this.isSelected != selected) {
      this.isSelected = selected;
      updateColors();
    }
  }

  public boolean isHighlighted() {
    return isHighlighted;
  }

  public void setHighlighted(boolean highlighted) {
    if (this.isHighlighted != highlighted) {
      this.isHighlighted = highlighted;
      updateColors();
    }
  }

  public void setTimeSliderVisible(boolean visible) {
    if (1==1) return;
    if (visible) {
      if (timeSlider == null) {
        timeSlider = new CentroidTimeSlider(this);
        addChild(timeSlider);
      }
    } else {
      if (timeSlider != null) {
        removeChild(timeSlider);
        timeSlider = null;
      }
    }
  }

  private void updateColors() {
    FlowstratesStyle style = view.getStyle();
    if (isHighlighted) {
      setPaint(style.getMapAreaHighlightedCentroidColor());
      labelText.setPaint(style.getMapAreaHighlightedCentroidLabelColor());
      labelText.setTextPaint(style.getMapAreaHighlightedCentroidLabelTextColor());
    } else if (isSelected) {
      setPaint(style.getMapAreaSelectedCentroidPaint());
      labelText.setPaint(style.getMapAreaSelectedCentroidLabelPaint());
      labelText.setTextPaint(style.getMapAreaSelectedCentroidLabelTextPaint());
    } else {
      setPaint(style.getMapAreaCentroidPaint());
      labelText.setPaint(style.getMapAreaCentroidLabelPaint());
      labelText.setTextPaint(style.getMapAreaCentroidLabelTextPaint());
    }
  }

  public PText getLabelNode() {
    return labelText;
  }

//  @Override
//  public void setPaint(Paint newPaint) {
//    super.setPaint(newPaint);
//    if (labelNode != null) {
//      labelNode.setTextPaint(newPaint);
//    }
//  }

  @Override
  public void setPickable(boolean isPickable) {
    super.setPickable(isPickable);
    if (labelText != null) {
      labelText.setPickable(isPickable);
    }
  }

  @Override
  public boolean setBounds(double x, double y,
      double width, double height) {
    if (labelText != null) {
//      PNodes.setPosition(labelNode, x + size*1.5, y - labelNode.getFont().getSize2D()/2.0);
      PNodes.setPosition(labelText, x - labelText.getWidth()/2, y + size /*- labelNode.getFont().getSize2D()/2.0*/);
    }
    if (timeSlider != null) {
      PNodes.setPosition(timeSlider,
          timeSlider.getX() + (x - getX()), timeSlider.getY() + (y - getY()), true);
    }
    return super.setBounds(x, y, width, height);
  }

  public Point2D getPoint() {
    return (Point2D) point.clone();
  }

  void updateInCamera(PCamera cam) {
    Point2D p = getPoint();
    setVisible(cam.getViewBounds().contains(p));
//    labelNode.setVisible(cam.getBounds().contains(labelNode.getFullBounds()));
    cam.viewToLocal(p);
    p.setLocation(p.getX() - size/2, p.getY() - size/2);
    PNodes.setPosition(this, p);
  }

  @Override
  public void fullPaint(PPaintContext paintContext) {
    Graphics2D g2 = paintContext.getGraphics();
    Shape oldClip = g2.getClip();
    g2.setClip(getParent().getBounds());
    super.fullPaint(paintContext);
    g2.setClip(oldClip);
  }


}
