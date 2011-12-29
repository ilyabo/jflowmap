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

package jflowmap.models.map;

import java.awt.geom.Point2D;
import java.util.Arrays;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;

/**
 * @author Ilya Boyandin
 *     Date: 25-Sep-2009
 */
public class Polygon {

  private final Point2D[] exteriorPoints;

  public Polygon(Point2D[] points) {
    this.exteriorPoints = points.clone();
  }

  public boolean isEmpty() {
    return exteriorPoints.length == 0;
  }

  public Point2D[] getPoints() {
    return exteriorPoints.clone();
  }

  public static Polygon convert(com.vividsolutions.jts.geom.Polygon poly) {
    // TODO: load interior points as well
    return new Polygon(points(poly.getExteriorRing()));
  }

  private static Point2D[] points(LineString exterior) {
    Coordinate[] coords = exterior.getCoordinates();
    Point2D[] points = new Point2D[coords.length];
    for (int i = 0; i < coords.length; i++) {
      Coordinate c = coords[i];
      points[i] = new Point2D.Double(c.x, c.y);
    }
    return points;
  }

  @Override
  public String toString() {
    return "Polygon [points=" + Arrays.toString(exteriorPoints) + "]";
  }

}
