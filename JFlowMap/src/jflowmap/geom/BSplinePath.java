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

package jflowmap.geom;

import java.awt.geom.Path2D;
import java.util.List;


/**
 * Here a B-spline curve (which are not supported by Java2D)
 * is converted to a series of cubic Bezier curves that can
 * be rendered by Path2D.
 * <p>
 * Based on the BSpline example from the book
 * "Computer Graphics Using Java 2D and 3D"
 * by Hong Zhang and Y.Daniel Liang.
 *
 * @author Ilya Boyandin
 */
public class BSplinePath extends Path2D.Double {

  private static final long serialVersionUID = 3838887297097056901L;

  public BSplinePath(List<Point> points) {
    int n = points.size();
    if (n < 4) {
      throw new IllegalArgumentException(
          "BSplinePath needs at least 4 points");
    }

    Point p1 = null;
    Point p2 = null;
    Point p3 = null;
    double x1, y1, x2, y2, x3, y3, x4, y4;

    p1 = points.get(0);
    moveTo(p1.x(), p1.y());
    p1 = points.get(1);
    p2 = points.get(2);
    p3 = points.get(3);
    x1 = p1.x();
    y1 = p1.y();
    x2 = (p1.x() + p2.x()) / 2.0f;
    y2 = (p1.y() + p2.y()) / 2.0f;
    x4 = (2.0f * p2.x() + p3.x()) / 3.0f;
    y4 = (2.0f * p2.y() + p3.y()) / 3.0f;
    x3 = (x2 + x4) / 2.0f;
    y3 = (y2 + y4) / 2.0f;
    curveTo(x1, y1, x2, y2, x3, y3);
    for (int i = 2; i < n - 4; i++) {
      p1 = p2;
      p2 = p3;
      p3 = points.get(i + 2);
      x1 = x4;
      y1 = y4;
      x2 = (p1.x() + 2.0f * p2.x()) / 3.0f;
      y2 = (p1.y() + 2.0f * p2.y()) / 3.0f;
      x4 = (2.0f * p2.x() + p3.x()) / 3.0f;
      y4 = (2.0f * p2.y() + p3.y()) / 3.0f;
      x3 = (x2 + x4) / 2.0f;
      y3 = (y2 + y4) / 2.0f;
      curveTo(x1, y1, x2, y2, x3, y3);
    }
    p1 = p2;
    p2 = p3;
    p3 = points.get(n - 2);
    x1 = x4;
    y1 = y4;
    x2 = (p1.x() + 2.0f * p2.x()) / 3.0f;
    y2 = (p1.y() + 2.0f * p2.y()) / 3.0f;
    x4 = (p2.x() + p3.x()) / 2.0f;
    y4 = (p2.y() + p3.y()) / 2.0f;
    x3 = (x2 + x4) / 2.0f;
    y3 = (y2 + y4) / 2.0f;
//    curveTo(x1, y1, x2, y2, x3, y3);      // TODO: why does this cause a "tail" to be painted?
    p2 = p3;
    p3 = points.get(n - 1);
    x1 = x4;
    y1 = y4;
    x2 = p2.x();
    y2 = p2.y();
    x3 = p3.x();
    y3 = p3.y();
//    curveTo(x1, y1, x2, y2, x3, y3);
    curveTo(x1, y1, x3, y3, x3, y3);
  }

}
