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

package jflowmap.views;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * @author Ilya Boyandin
 */
public class PTooltip extends PPath {

  private static final long serialVersionUID = 4699401260245122226L;
  private static final Color DEFAULT_PAINT = new Color(0, 55, 80, 225);
  private static final Color DEFAULT_TEXT_PAINT = Color.white;
  private static final Color DEFAULT_STROKE_PAINT = new Color(0, 0, 0, 100);
  private static final BasicStroke DEFAULT_STROKE = new BasicStroke(.5f);
  private static Font HEADER_FONT = new Font("Arial", Font.PLAIN, 12);
  private static Font LABELS_FONT = HEADER_FONT;
  private static Font VALUES_FONT = HEADER_FONT;
  private final Point padding;
  private int gap;
  private final PText headerNode;
  private final PText labelsNode;
  private final PText valuesNode;

  public PTooltip(int archw, int archh) {
    super(new RoundRectangle2D.Double(0, 0, 100, 100, archw, archh));
    padding = new Point(5, 5);
    gap = 3;

    headerNode = new PText();
    labelsNode = new PText();
    valuesNode = new PText();

    headerNode.setFont(HEADER_FONT);
    labelsNode.setFont(LABELS_FONT);
    valuesNode.setFont(VALUES_FONT);

    addChild(headerNode);
    addChild(labelsNode);
    addChild(valuesNode);

    setPaint(DEFAULT_PAINT);
//    if (CGVis.IS_OS_MAC) {
//      setStroke(null);
//    } else {
      setStroke(DEFAULT_STROKE);
      setStrokePaint(DEFAULT_STROKE_PAINT);
//    }
    setTextPaint(DEFAULT_TEXT_PAINT);

    setPickable(false);
  }

  public PTooltip() {
    this(10, 20);
  }

  @Override
  public void setParent(PNode newParent) {
    if (newParent != null  &&  !(newParent instanceof PCamera)) {
      throw new IllegalArgumentException("Tooltip can only be added to a camera.");
    }
    super.setParent(newParent);
  }

  public void setTextPaint(Paint textPaint) {
    headerNode.setTextPaint(textPaint);
    labelsNode.setTextPaint(textPaint);
    valuesNode.setTextPaint(textPaint);
  }

  public void setPadding(int px, int py) {
    this.padding.x = px;
    this.padding.y = py;
    updateBounds();
  }

  public Point getPadding() {
    return (Point) padding.clone();
  }

  public int getGap() {
    return gap;
  }

  public void setGap(int gap) {
    this.gap = gap;
  }

  public void setText(String header, String labels, String values) {
    headerNode.setText(header);
    labelsNode.setText(labels);
    valuesNode.setText(values);
    updateBounds();
  }

  public boolean setPosition(double x, double y) {
    final PBounds b = getBoundsReference();
    return setBounds(x, y, b.width, b.height);
  }

  private void updateBounds() {
    if (headerNode != null && labelsNode != null && valuesNode != null) {
      final PBounds hb = headerNode.getBoundsReference();
      final PBounds lb = labelsNode.getBoundsReference();
      final PBounds vb = valuesNode.getBoundsReference();
      super.setBounds(getX(), getY(), Math.max(hb.width, lb.width + gap
          + vb.width)
          + padding.x * 2, hb.height + gap
          + Math.max(lb.height, vb.height) + padding.y * 2);
      final PBounds b = getBoundsReference();
      headerNode.setBounds(b.x + padding.x, b.y + padding.y, hb.width,
          hb.height);
      labelsNode.setBounds(b.x + padding.x, hb.height + gap + b.y
          + padding.y, lb.width, lb.height);
      valuesNode.setBounds(b.x + padding.x + lb.width + gap, hb.height
          + gap + b.y + padding.y, vb.width, vb.height);
    }
  }

  @Override
  public boolean setBounds(double x, double y, double width, double height) {
    if (super.setBounds(x, y, width, height)) {
      updateBounds();
      return true;
    }
    return false;
  }

  @Override
  protected void paint(PPaintContext pc) {
    final int oldQuality = pc.getRenderQuality();
    if (oldQuality != PPaintContext.HIGH_QUALITY_RENDERING) {
      pc.setRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
    }

    super.paint(pc);

    if (oldQuality != PPaintContext.HIGH_QUALITY_RENDERING) {
      pc.setRenderQuality(oldQuality);
    }
  }


  public PCamera getCamera() {
    return (PCamera)getParent();
  }

  /**
   * This method places the tooltip so that it is still visible on the screen
   * even if the point is close to the edge.
   */
  public void showTooltipAt(double x, double y, double dx, double dy) {
    PCamera camera = getCamera();
    PBounds cameraBounds = camera.getBoundsReference();
    PBounds tooltipBounds = getBoundsReference();

    Point2D pos = new Point2D.Double(x, y);
    camera.viewToLocal(pos);
    x = pos.getX();
    y = pos.getY();
    if (x + tooltipBounds.getWidth() > cameraBounds.getWidth()) {
      final double _x = pos.getX() - tooltipBounds.getWidth() - dx;
      if (cameraBounds.getX() - _x < x + tooltipBounds.getWidth() - cameraBounds.getMaxX()) {
        x = _x;
      }
    }
    if (y + tooltipBounds.getHeight() > cameraBounds.getHeight()) {
      final double _y = pos.getY() - tooltipBounds.getHeight() - dy;
      if (cameraBounds.getY() - _y < y + tooltipBounds.getHeight() - cameraBounds.getMaxY()) {
        y = _y;
      }
    }
    pos.setLocation(x + dx, y + dy);
    setPosition(pos.getX(), pos.getY());
    setVisible(true);
  }


}
