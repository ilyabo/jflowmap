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

import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

import com.google.common.collect.Iterables;

/**
 * @author Ilya Boyandin
 *     Date: 25-Sep-2009
 */
public class Area {

  private final String id;
  private final String name;
  private final Polygon[] polygons;

  public Area(String id, String name, Iterable<Polygon> polygons) {
    this.id = id;
    this.name = name;
    this.polygons = Iterables.toArray(polygons, Polygon.class);
  }

  public Area(String id, String name, Polygon[] polygons) {
    this.id = id;
    this.name = name;
    this.polygons = polygons.clone();
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public Polygon[] getPolygons() {
    return polygons.clone();
  }

  public Path2D asPath() {
    GeneralPath path = new GeneralPath();
    for (Polygon poly : polygons) {
      Point2D[] points = poly.getPoints();
      path.moveTo((float)points[0].getX(), (float)points[0].getY());
      for (int i = 1; i < points.length; i++) {
        path.lineTo((float)points[i].getX(), (float)points[i].getY());
      }
    }
    return path;
  }
}
