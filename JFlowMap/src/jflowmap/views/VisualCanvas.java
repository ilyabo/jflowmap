/*
 * This file is part of JFlowMap.
 *
 * Copyright 2009 Ilya Boyandin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jflowmap.views;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import jflowmap.util.piccolo.PanHandler;
import jflowmap.util.piccolo.PiccoloUtils;
import jflowmap.util.piccolo.ZoomHandler;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * @author Ilya Boyandin
 */
public class VisualCanvas extends PCanvas {

  private static final Dimension MIN_SIZE = new Dimension(150, 100);
  private final ZoomHandler zoomHandler;
  private boolean autoFitOnBoundsChange = true;

  public VisualCanvas() {
    setZoomEventHandler(null);
    zoomHandler = createZoomHandler();
    addInputEventListener(zoomHandler);
    setPanEventHandler(new PanHandler());
  }

  public ZoomHandler getZoomHandler() {
    return zoomHandler;
  }

  protected ZoomHandler createZoomHandler() {
    return new ZoomHandler();
  }

  public boolean getAutoFitOnBoundsChange() {
    return autoFitOnBoundsChange;
  }

  public void setAutoFitOnBoundsChange(boolean autoFitOnBoundsChange) {
    this.autoFitOnBoundsChange = autoFitOnBoundsChange;
  }

  public void setViewZoomPoint(Point2D point) {
    zoomHandler.setFixedViewZoomPoint(point);
  }

  public void setMinZoomScale(double minScale) {
    zoomHandler.setMinScale(minScale);
  }

  public void setMaxZoomScale(double maxScale) {
    zoomHandler.setMaxScale(maxScale);
  }

  public void fitChildrenInCameraView() {
    PCamera camera = getCamera();
    PBounds boundRect = getLayer().getFullBounds();
    camera.globalToLocal(boundRect);
    camera.animateViewToCenterBounds(boundRect, true, 0);
  }

  @Override
  public Dimension getMinimumSize() {
    return MIN_SIZE;
  }

  @Override
  public void setBounds(int x, int y, int w, int h) {
    if (autoFitOnBoundsChange) {
      Rectangle2D oldVisibleBounds =  getVisibleBounds();
      Rectangle2D oldContentBounds = getViewContentBounds();

      super.setBounds(x, y, w, h);

      if (oldVisibleBounds != null) {
        fitVisibleInCameraView(oldVisibleBounds, oldContentBounds);
      }
    } else {
      super.setBounds(x, y, w, h);
    }
  }

  private static final Insets NULL_INSETS = new Insets(0, 0, 0, 0);

  public void fitVisibleInCameraView(Rectangle2D visibleBounds, Rectangle2D contentBounds) {
    if (contentBounds.getWidth() <= 0 || contentBounds.getHeight() <= 0) {
      return;
    }
    final Insets insets;
//    if (contentBounds.contains(visibleBounds)) { // the whole  is inside the view
//      insets = getContentInsets();
//      insets.left += 5;
//      insets.top += 5;
//      insets.bottom += 5;
//      insets.right += 5;
//    } else {
      insets = NULL_INSETS;
//    }
    PiccoloUtils.setViewPaddedBounds(getCamera(), visibleBounds, insets);
  }


  /**
   * Bounds of the part of the visualization (all of the Nodes together) that
   * is currently visible in the view (in the view coordinate system).
   *
   * @return
   */
  public Rectangle2D getVisibleBounds() {
//      final PBounds mb = getBounds();
      final PBounds mb = getLayer().getFullBounds();
      if (mb != null) {
          final PBounds vb = getCamera().getViewBounds();
          Rectangle2D.intersect(vb, mb, vb);
          return vb;
      }
      return null;
  }

  /**
   * Bounds of the area where the visualization should be placed (without the floating
   * panels) in the camera coordinate system.
   *
   * @return
   */
  private Rectangle2D getContentBounds() {
//    final Insets insets = getContentInsets();
//    final PBounds vb = getCamera().getBounds();
//    vb.x += insets.left;
//    vb.y += insets.top;
//    vb.width -= insets.left + insets.right;
//    vb.height -= insets.top + insets.bottom;
//    return vb;
    return getBounds();
  }

  private Rectangle2D getViewContentBounds() {
    final Rectangle2D cb = getContentBounds();
    getCamera().localToView(cb);
    return cb;
  }
}
