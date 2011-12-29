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
public class FPoint {

  private final Point point;
  private final boolean fixed;

  public FPoint(double x, double y, boolean fixed) {
    this.point = new Point(x, y);
    this.fixed = fixed;
  }

  public FPoint(Point point, boolean fixed) {
    this.point = point;
    this.fixed = fixed;
  }

  public double x() {
    return point.x();
  }

  public double y() {
    return point.y();
  }

  public Point getPoint() {
    return point;
  }

  public boolean isFixed() {
    return fixed;
  }

  public FPoint withFixed(boolean fixed) {
    if (fixed == this.fixed) {
      return this;
    }
    return new FPoint(point, fixed);
  }

  public FPoint withX(double x) {
    if (x == point.x()) {
      return this;
    }
    return new FPoint(x, point.y(), fixed);
  }

  public FPoint withY(double y) {
    if (y == point.y()) {
      return this;
    }
    return new FPoint(point.x(), y, fixed);
  }

  public double distanceTo(FPoint other) {
    return point.distanceTo(other.point);
  }

  public Point2D asPoint2D() {
    return new Point2D.Double(x(), y());
  }

  public static FPoint valueOf(Point2D point, boolean fixed) {
    return new FPoint(point.getX(), point.getY(), fixed);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (fixed ? 1231 : 1237);
    result = prime * result + ((point == null) ? 0 : point.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    FPoint other = (FPoint) obj;
    if (fixed != other.fixed)
      return false;
    if (point == null) {
      if (other.point != null)
        return false;
    } else if (!point.equals(other.point))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "FPoint [fixed=" + fixed + ", x=" + point.x() + ", y=" + point.y() + "]";
  }

}
