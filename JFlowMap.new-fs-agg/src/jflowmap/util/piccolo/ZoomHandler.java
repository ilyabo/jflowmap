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

package jflowmap.util.piccolo;

import java.awt.geom.Point2D;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.event.PDragSequenceEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;

/**
 * @author Ilya Boyandin
 */
public class ZoomHandler extends PDragSequenceEventHandler {

  private static final double WHEEL_ZOOM_UNIT = 0.15;

  private double minScale = 0;
  private double maxScale = Double.MAX_VALUE;
  private Point2D viewZoomPoint;
  private boolean zoomByDragging = false;
  private boolean fixZoomPoint = false;

  public ZoomHandler() {
    this(0, Double.MAX_VALUE, false);
  }

  public ZoomHandler(double minScale, double maxScale, boolean zoomByDragging) {
    this.minScale = minScale;
    this.maxScale = maxScale;
    this.zoomByDragging = zoomByDragging;
  }

  public double getMaxScale() {
    return maxScale;
  }

  public void setMaxScale(double maxScale) {
    this.maxScale = maxScale;
  }

  public double getMinScale() {
    return minScale;
  }

  public void setMinScale(double minScale) {
    this.minScale = minScale;
  }

  public Point2D getViewZoomPoint() {
    return viewZoomPoint;
  }

  public boolean isZooming() {
    return viewZoomPoint != null;
  }

  public void setFixedViewZoomPoint(Point2D viewZoomPoint) {
    this.viewZoomPoint = viewZoomPoint;
    this.fixZoomPoint = (viewZoomPoint != null);
  }

  @Override
  public void mouseWheelRotated(PInputEvent event) {
    final PCamera c = event.getCamera();
    final Point2D position;
    if (viewZoomPoint != null) {
      position = viewZoomPoint;
    } else {
      position = event.getPosition();
    }
    double scale = 1.0 - event.getWheelRotation() * WHEEL_ZOOM_UNIT;
    zoom(c, scale, position);
  }

  protected void zoom(PCamera c, double scale, Point2D position) {
    c.scaleViewAboutPoint(
        checkScaleConstraints(c.getViewScale(), scale),
        position.getX(), position.getY());
  }

  @Override
  protected void dragActivityFirstStep(PInputEvent aEvent) {
    if (zoomByDragging) {
      if (aEvent.isControlDown() && aEvent.isLeftMouseButton()) {
        if (viewZoomPoint == null) {
          viewZoomPoint = aEvent.getPosition();
        }
      }
    }
  }

  @Override
  protected void dragActivityStep(PInputEvent event) {
    if (zoomByDragging) {
      if (event.isControlDown() && event.isLeftMouseButton()) {
        if (viewZoomPoint == null) {
          viewZoomPoint = event.getPosition();
        }
        final PCamera c = event.getCamera();
        final double dy = -(event.getCanvasPosition().getY() - getMousePressedCanvasPoint().getY());
        final double scaleDelta = checkScaleConstraints(c.getViewScale(), 1.0 + (0.001 * dy));

        c.scaleViewAboutPoint(scaleDelta, viewZoomPoint.getX(),
            viewZoomPoint.getY());
      }
    }
  }

  protected double checkScaleConstraints(final double currentScale,
      double scaleDelta) {
    final double newScale = currentScale * scaleDelta;
    final double minScale = getMinScale();
    final double maxScale = getMaxScale();
    if (newScale < minScale) {
      scaleDelta = minScale / currentScale;
    }
    if ((maxScale > 0) && (newScale > maxScale)) {
      scaleDelta = maxScale / currentScale;
    }
    return scaleDelta;
  }

  @Override
  protected void dragActivityFinalStep(PInputEvent aEvent) {
    if (zoomByDragging) {
      if (!fixZoomPoint) {
        viewZoomPoint = null;
      }
    }
  }

}
