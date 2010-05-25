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


/**
 * @author Ilya Boyandin
 */
public class Vector2D {

  public final static Vector2D ZERO = new Vector2D(0, 0);
  private final double x;
  private final double y;
  private double length = Double.NaN;

  public Vector2D(double x, double y) {
    this.x = x;
    this.y = y;
  }
  
  public double x() {
    return x;
  }
  
  public double y() {
    return y;
  }
  
  public boolean isNaN() {
    return Double.isNaN(x)  ||  Double.isNaN(y);
  }
  
  public boolean isZero() {
    return isZero(0.0);
  }
  
  public boolean isZero(double eps) {
    return Math.abs(x) < eps  &&  Math.abs(y) < eps;
  }

  public double dot(Vector2D b) {
    return x * b.x + y * b.y;
  }

  public double distanceTo(Vector2D that) {
    return this.minus(that).length();
  }
  
  public Vector2D plus(Vector2D b) {
    return new Vector2D(x + b.x, y + b.y);
  }
  
  public Vector2D minus(Vector2D b) {
    return new Vector2D(x - b.x, y - b.y);
  }

  public double length() {
    if (Double.isNaN(length)) {
      length = Math.sqrt(x * x + y * y);
    }
    return length;
  }
  
  public Vector2D times(double factor) {
    return new Vector2D(x * factor, y * factor);
  }

  public Vector2D direction() {
    return times(1.0 / length());
  }

  public static Vector2D valueOf(Point2D point) {
    return new Vector2D(point.getX(), point.getY());
  }

  public static Vector2D valueOf(Point2D.Double startPoint, Point2D.Double endPoint) {
    return new Vector2D(endPoint.getX() - startPoint.getX(), endPoint.getY() - startPoint.getY());
  }
  
  public static Vector2D valueOf(Point startPoint, Point endPoint) {
    return new Vector2D(endPoint.x() - startPoint.x(), endPoint.y() - startPoint.y());
  }

  public Point2D.Double movePoint(Point2D.Double point) {
    return new Point2D.Double(point.x + x, point.y + y);
  }
  
  @Override
  public String toString() {
    return "[Vector2D:" +
        " x = " + x + 
        ", y = " + y + 
    		"]";
  }
}
