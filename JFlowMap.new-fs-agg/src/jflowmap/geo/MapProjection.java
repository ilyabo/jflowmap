package jflowmap.geo;

import java.awt.geom.Point2D;

/**
 * @author Ilya Boyandin
 */
public interface MapProjection {

  Point2D project(double lon, double lat);

}
