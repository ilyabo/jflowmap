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
import java.awt.geom.Rectangle2D;
import java.util.List;

import jflowmap.geo.MapProjection;

import org.apache.log4j.Logger;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.Geometry;

/**
 * @author Ilya Boyandin
 *     Date: 25-Sep-2009
 */
public class MapArea {

  private static Logger logger = Logger.getLogger(MapArea.class);

  private final String id;
  private final String name;
  private final Polygon[] polygons;

  public MapArea(String id, String name, Iterable<Polygon> polygons) {
    this(id, name, Iterables.toArray(polygons, Polygon.class), false);
  }

  public MapArea(String id, String name, Polygon[] polygons) {
    this(id, name, polygons, true);
  }

  private MapArea(String id, String name, Polygon[] polygons, boolean clone) {
    this.id = id;
    this.name = name;
    if (clone) {
      this.polygons = polygons.clone();
    } else {
      this.polygons = polygons;
    }
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

  public boolean isEmpty() {
    for (Polygon p : polygons) {
      if (!p.isEmpty()) return false;
    }
    return true;
  }

  public Path2D asPath(MapProjection proj) {
    GeneralPath path = new GeneralPath();
    for (Polygon poly : polygons) {
      Point2D[] points = poly.getPoints();
      Point2D startP = proj.project(points[0].getX(), points[0].getY());
      path.moveTo(startP.getX(), startP.getY());
      for (int i = 1; i < points.length; i++) {
        Point2D p = proj.project(points[i].getX(), points[i].getY());
        path.lineTo(p.getX(), p.getY());
      }
    }
    return path;
  }

  public Rectangle2D asBoundingBox(MapProjection proj) {
    Rectangle2D.Double bb = null;
    for (Polygon poly : polygons) {
      for (Point2D point : poly.getPoints()) {
        Point2D p = proj.project(point.getX(), point.getY());
        if (bb == null) {
          bb = new Rectangle2D.Double(p.getX(), p.getY(), 0, 0);
        } else {
          if (!bb.contains(p)) {
            bb.add(p);
          }
        }
      }
    }
    return bb;
  }

  public static MapArea fromGeometry(String id, String name, Geometry g) {
    List<Polygon> list = Lists.newArrayList();

    if (g.getGeometryType().equals("Polygon")) {
      list.add(Polygon.convert((com.vividsolutions.jts.geom.Polygon)g));
    } else if (g.getGeometryType().equals("MultiPolygon")) {
      for (int i = 0; i < g.getNumGeometries(); i++) {
        list.add(Polygon.convert((com.vividsolutions.jts.geom.Polygon)g.getGeometryN(i)));
      }
    } else {
      logger.warn("Skipping unsupported geometry type: " + g.getGeometryType());
    }

    return new MapArea(id, name, list);
  }

}
