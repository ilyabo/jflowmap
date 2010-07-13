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

package jflowmap.views.timeline;

import java.awt.Paint;
import java.awt.geom.Arc2D;

import edu.umd.cs.piccolo.nodes.PPath;

/**
 * @author Ilya Boyandin
 */
public class HalfMoonsNode extends PPath {

  public HalfMoonsNode(
      double cellWidth, double cellHeight,
      double leftNormalizedValue, double rightNormalizedValue,
      Paint leftPaint, Paint rightPaint) {
    addChild(createHalfCircle(0, 0, cellWidth, cellHeight, true, leftNormalizedValue, leftPaint));
    addChild(createHalfCircle(0, 0, cellWidth, cellHeight, false, rightNormalizedValue, rightPaint));
  }

  private PPath createHalfCircle(double x, double y,
      double cellWidth, double cellHeight,
      boolean leftNotRight, double normalizedValue, Paint paint) {
    double wh = Math.min(cellWidth, cellHeight);
    double r = Math.sqrt(Math.abs(normalizedValue)) * wh;
    double off = (wh - r)/2;
    PPath ppath = new PPath(new Arc2D.Double(x + off, y + off, r, r, (leftNotRight ? 90 : -90), 180, Arc2D.PIE));
    ppath.setStroke(null);
    ppath.setPaint(paint);
    return ppath;
  }

}
