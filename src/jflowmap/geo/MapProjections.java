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

package jflowmap.geo;

import java.awt.geom.Point2D;

import jflowmap.geom.Point;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

/**
 * @author Ilya Boyandin
 */
public enum MapProjections implements MapProjection {

  NONE {
    @Override
    public Point2D project(double lon, double lat) {
      return new Point2D.Double(lon, lat);
    }
  },

  FLIPY {
    @Override
    public Point2D project(double lon, double lat) {
      return new Point2D.Double(lon, -lat);
    }
  },

  MERCATOR {
    // if the scale is smaller than 100 short edges will not be visible
    private final static double SCALE = 100;
    private final static boolean INVERT_Y = true;

    @Override
    public Point2D project(double lon, double lat) {
      return new Point2D.Double(
          SCALE * lon / 180,
          SCALE * (INVERT_Y ? -1 : 1) * (lat > 85 ?
              1 : (lat < -85 ?
                  -1 : Math.log(Math.tan(Math.PI / 4 + radians(lat) / 2)) / Math.PI)
              ));
    }

  },

  WINKELTRIPEL {
    private final double phi1 = Math.acos(2/Math.PI);
    private final double cos_phi1 = Math.cos(phi1);

    public Point2D project(double lat, double lon) {
      double lplam = radians(lat);
      double lpphi = radians(lon);

      double c = 0.5 * lplam;
      double cos_lpphi = Math.cos(lpphi);
      double alpha = Math.acos(cos_lpphi * Math.cos(c));

      double x, y;

      if (alpha != 0) {
        double sinc_alpha = sinc(alpha);
        x = 2.0 * cos_lpphi * Math.sin(c) / sinc_alpha;
        y = Math.sin(lpphi) / sinc_alpha;
      } else {
        x = y = 0.0;
      }

      x = (x + lplam * cos_phi1) * 0.5;
      y = (y + lpphi) * 0.5;

      y = -y;

      return new Point2D.Double(x, y);
    }

    private double sinc(double x) { return Math.sin(x)/x; }

  }

  ;


  private static double radians(double degrees) {
    return degrees * Math.PI / 180;
  }


  public static Iterable<Point> projectAll(Iterable<Point> points, final MapProjection mapProjection) {
    return Iterables.transform(points, new Function<Point, Point>() {
      @Override
      public Point apply(Point from) {
        return Point.valueOf(mapProjection.project(from.x(), from.y()));
      }
    });
  }

}
