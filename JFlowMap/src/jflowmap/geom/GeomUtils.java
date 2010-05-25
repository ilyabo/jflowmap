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

import java.awt.geom.Point2D;
import java.util.Iterator;


/**
 * @author Ilya Boyandin
 */
public final class GeomUtils {

  private GeomUtils() {
  }

  public static boolean isSelfLoopEdge(double srcX, double srcY, double targetX, double targetY) {
    return srcX == targetX  &&  srcY == targetY;
  }

  public static Point centroid(Iterator<Point> points) {
    double x = 0, y = 0;
    int count = 0;
    while (points.hasNext()) {
      Point p = points.next();
      x += p.x();
      y += p.y();
      count++;
    }
    x /= count;
    y /= count;
    return new Point(x, y);
  }

  public static double distance(double x1, double y1, double x2, double y2) {
    double dx = x1 - x2;
    double dy = y1 - y2;
    return Math.sqrt(dx * dx + dy * dy);
  }

  public static Point2D projectPointToLine(Point2D line1, Point2D line2, Point2D point) {
    return projectPointToLine(
        line1.getX(), line1.getY(), line2.getX(), line2.getY(),
        point.getX(), point.getY()).asPoint2D();
  }

  public static Point projectPointToLine(Point line1, Point line2, Point point) {
    return projectPointToLine(
        line1.x(), line1.y(), line2.x(), line2.y(),
        point.x(), point.y());
  }

  /**
   * See http://www.exaflop.org/docs/cgafaq/cga1.html
   */
  public static Point projectPointToLine(double x1, double y1, double x2, double y2, double x, double y) {
    double L = Math.sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1));
    double r = ((y1-y)*(y1-y2) - (x1-x)*(x2-x1)) / (L * L);
    return new Point(x1 + r * (x2-x1), y1 + r * (y2-y1));
  }

  public static Point2D midpoint(Point2D a, Point2D b) {
    return between(a, b, 0.5);
  }

  /**
   * Returns a point on a segment between the two points
   * @param alpha Between 0 and 1
   */
  public static Point2D between(Point2D a, Point2D b, double alpha) {
    return new Point2D.Double(
        a.getX() + (b.getX() - a.getX()) * alpha,
        a.getY() + (b.getY() - a.getY()) * alpha
    );
  }

  public static Point midpoint(Point a, Point b) {
    return between(a, b, 0.5);
  }

  /**
   * Returns a point on a segment between the two points
   * @param alpha Between 0 and 1
   */
  public static Point between(Point a, Point b, double alpha) {
    return new Point(
        a.x() + (b.x() - a.x()) * alpha,
        a.y() + (b.y() - a.y()) * alpha
    );
  }

}
