package jflowmap.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import jflowmap.geom.GeomUtils;
import jflowmap.geom.Point;

import org.junit.Test;

public class GeomUtilsTest {

  private static final double EPS = 1e-7;

  @Test
  public void testProjectDistanceIterable() {
    assertEquals(1.0,
        GeomUtils.distance(
            Arrays.<Double>asList(1.0, 0.0, 0.0),
            Arrays.<Double>asList(0.0, 0.0, 0.0)), EPS);
    assertTrue(
        Double.isNaN(
          GeomUtils.distance(
              Arrays.<Double>asList(1.0, 0.0, 0.0),
              Arrays.<Double>asList(0.0, 0.0, Double.NaN))));
    assertEquals(Math.sqrt(8),
        GeomUtils.distance(
            Arrays.<Double>asList(1.0, 2.0, 3.0),
            Arrays.<Double>asList(3.0, 2.0, 1.0)), EPS);
  }

  @Test
  public void testProjectPointToLine() {
  	assertPointEquals(4, 0, GeomUtils.projectPointToLine(0, 0,  10, 0,   4, 1));
    assertPointEquals(-1, 0, GeomUtils.projectPointToLine(0, 0,  10, 0,   -1, 1));
    assertPointEquals(15, 0, GeomUtils.projectPointToLine(0, 0,  10, 0,   15, 15));
    assertPointEquals(3, 3, GeomUtils.projectPointToLine(1, 1,  10, 10,   2, 4));
    assertPointEquals(2, 4, GeomUtils.projectPointToLine(0, 2,  9, 11,   3, 3));
    assertPointEquals(-1, 1, GeomUtils.projectPointToLine(0, 2,  9, 11,   0, 0));
    assertPointEquals(7, 2, GeomUtils.projectPointToLine(3, 0,  5, 1,   6, 4));
    assertPointEquals(-3, 0, GeomUtils.projectPointToLine(-2, 1,  -5, -2,   -2, -1));
  }

  private void assertPointEquals(double expectedX, double expectedY, Point actual) {
    assertEquals(expectedX, actual.x(), EPS);
    assertEquals(expectedY, actual.y(), EPS);
  }

}
