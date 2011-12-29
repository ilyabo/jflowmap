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
import java.awt.geom.Rectangle2D;
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

  public static double distance(Iterable<Double> v1, Iterable<Double> v2) {
    return distance(v1, v2, Double.NaN);
  }

  public static double distance(Iterable<Double> v1, Iterable<Double> v2, double maxForNaNs) {
    Iterator<Double> it1 = v1.iterator();
    Iterator<Double> it2 = v2.iterator();
    double sum = 0;
    while (it1.hasNext()) {
      if (!it2.hasNext()) {
        throw new IllegalArgumentException("Vectors not of the same size");
      }
      double c1 = it1.next();
      double c2 = it2.next();
      double d;

//      if (Double.isNaN(c1)) c1 = 0;
//      if (Double.isNaN(c2)) c2 = 0;
//      d = c1 - c2;

      d = distance(c1, c2, maxForNaNs);

//    if (Double.isNaN(c1)) {
//    if (Double.isNaN(c2)) {
//      c1 = c2 = maxForNaNs;
//    } else {
//      c1 = Math.max(c2, maxForNaNs - c2);
//    }
//  } else if (Double.isNaN(c2)) {
//    c2 = Math.max(c1, maxForNaNs - c1);
//  }
//  d = c1 - c2;
      sum += d * d;
    }
    if (it2.hasNext()) {
      throw new IllegalArgumentException("Vectors not of the same size");
    }
    return Math.sqrt(sum);
  }

  public static double distance(double c1, double c2, double maxForNaNs) {
    double d;
    if (Double.isNaN(maxForNaNs)) {
      d = c1 - c2;
    } else {
      if (Double.isNaN(c1)) {
        if (Double.isNaN(c2)) {
          d = maxForNaNs;
        } else {
          d = Math.max(c2, maxForNaNs - c2);
        }
      } else if (Double.isNaN(c2)) {
        d = Math.max(c1, maxForNaNs - c1);
      } else {
        d = c1 - c2;
      }
    }
    return d;
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

  public static Rectangle2D growRect(Rectangle2D rect, double d) {
    return growRect(rect, d, d, d, d);
  }

  public static Rectangle2D growRect(Rectangle2D rect, double top, double right, double bottom, double left) {
    Rectangle2D r = new Rectangle2D.Double(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
    growRectInPlace(r, top, right, bottom, left);
    return r;
  }

  public static void growRectInPlace(Rectangle2D rect, double top, double right, double bottom, double left) {
    rect.setRect(
        rect.getX() - left,
        rect.getY() - top,
        rect.getWidth() + left + right,
        rect.getHeight() + top + bottom
    );
  }

  public static void growRectInPlaceByRelativeSize(Rectangle2D rect, double top, double right, double bottom, double left) {
    rect.setRect(
        rect.getX() - rect.getWidth() * left,
        rect.getY() - rect.getHeight() * top,
        rect.getWidth() * (1 + left + right),
        rect.getHeight() * (1 + top + bottom)
    );
  }

  public static Rectangle2D growRectByRelativeSize(Rectangle2D rect, double top, double right, double bottom, double left) {
    Rectangle2D r = new Rectangle2D.Double(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
    growRectInPlaceByRelativeSize(r, top, right, bottom, left);
    return r;
  }

  public static double area(Rectangle2D rect) {
    return rect.getWidth() * rect.getHeight();
  }

}
