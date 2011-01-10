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

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Stroke;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;

import jflowmap.JFlowMapMain;


import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.util.PAffineTransform;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PDimension;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolox.util.PFixedWidthStroke;

/**
 * @author Ilya Boyandin
 */
public class PiccoloUtils {

  /**
   * @param camera
   * @param newBounds
   *      Target bounds in view coordinate system
   * @param cameraPadding
   *      Padding in camera coordinate system
   * @param duration
   */
  public static void animateViewToPaddedBounds(PCamera camera,
      Rectangle2D newBounds, Insets cameraPadding, long duration) {
    final PBounds viewBounds = camera.getViewBounds();

    final Dimension2D leftAndTop = camera.localToView(new Dimension(
        cameraPadding.left, cameraPadding.top));
    final Dimension2D rightAndBottom = camera.localToView(new Dimension(
        cameraPadding.right, cameraPadding.bottom));

    viewBounds.x += leftAndTop.getWidth();
    viewBounds.y += leftAndTop.getHeight();
    viewBounds.width -= leftAndTop.getWidth() + rightAndBottom.getWidth();
    viewBounds.height -= leftAndTop.getHeight()
        + rightAndBottom.getHeight();

    final PDimension delta = viewBounds.deltaRequiredToCenter(newBounds);
    final PAffineTransform newTransform = camera.getViewTransform();
    newTransform.translate(delta.width, delta.height);

    final double s = Math.min(viewBounds.getWidth() / newBounds.getWidth(),
        viewBounds.getHeight() / newBounds.getHeight());
    if (s != Double.POSITIVE_INFINITY && s != 0) {
      newTransform.scaleAboutPoint(s, newBounds.getCenterX(), newBounds
          .getCenterY());
    }

    camera.animateViewToTransform(newTransform, duration);
  }

  /**
   * @param camera
   * @param newBounds
   *      Target bounds in view coordinate system
   * @param cameraPadding
   *      Padding in camera coordinate system
   */
  public static void setViewPaddedBounds(PCamera camera,
      Rectangle2D newBounds, Insets cameraPadding) {
    animateViewToPaddedBounds(camera, newBounds, cameraPadding, 0);
  }

  /**
   * Workaround for Mac OS X Java, which doesn't support custom strokes at the
   * time of writing. This method prepares the specified stroke for the
   * specified paint context, returning a new instance of <code>Stroke</code>
   * with a scaled line width if necessary.
   *
   * @author Original version was written by Jeff Yoshimi and heuermh
   * @param stroke
   *      stroke to prepare, must not be null
   * @param paintContext
   *      paint context, must not be null
   * @return the specified stroke or a new instance of <code>Stroke</code>
   *     with a scaled line width if necessary
   */
  public static Stroke prepareStroke(final Stroke stroke,
      final PPaintContext paintContext) {
    if (stroke == null) {
      throw new IllegalArgumentException("stroke must not be null");
    }
    if (paintContext == null) {
      throw new IllegalArgumentException("paintContext must not be null");
    }

    // use the existing stroke on platforms other than MacOSX
    if (!JFlowMapMain.IS_OS_MAC) {
      return stroke;
    }

    // create a scaled BasicStroke for PFixedWidthStrokes on MacOSX
    if (stroke instanceof PFixedWidthStroke) {
      final PFixedWidthStroke fwStroke = (PFixedWidthStroke) stroke;
      final float lineWidth = (float) (fwStroke.getLineWidth() / paintContext
          .getScale());
      return new BasicStroke(lineWidth, fwStroke.getEndCap(), fwStroke
          .getLineJoin(), fwStroke.getMiterLimit(), fwStroke
          .getDashArray(), fwStroke.getDashPhase());
    }

    if (stroke instanceof BasicStroke) {
      final double scale = paintContext.getScale();

      // use the existing stroke on MacOSX at scales >= 1.0d
      if (scale >= 1.0) {
        return stroke;
      }

      // return a new instance of the specified stroke after scaling line
      // width
      BasicStroke bStroke = (BasicStroke) stroke;
      final float lineWidth = (float) (bStroke.getLineWidth() / scale);
      return new BasicStroke(lineWidth, bStroke.getEndCap(), bStroke
          .getLineJoin(), bStroke.getMiterLimit(), bStroke
          .getDashArray(), bStroke.getDashPhase());
    }

    // give up, custom strokes aren't supported on Mac OSX
    return stroke;
  }


}
