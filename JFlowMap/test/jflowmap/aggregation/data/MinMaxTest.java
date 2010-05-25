package jflowmap.aggregation.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;

import jflowmap.data.MinMax;

import org.junit.Test;

public class MinMaxTest {

  private static final double EPS = 1e-7;

  @Test
  public void test_normalizeAroundZero() {
    MinMax minMax = MinMax.createFor(Arrays.asList(-5.0, 0.0, 0.1, 1.4, 10.0).iterator());

    assertEquals(-0.5, minMax.normalizeAroundZero(-5), EPS);
    assertEquals(-0.25, minMax.normalizeAroundZero(-2.5), EPS);
    assertEquals(0.0, minMax.normalizeAroundZero(0.0), EPS);
    assertEquals(0.5, minMax.normalizeAroundZero(5.0), EPS);
    assertEquals(1.0, minMax.normalizeAroundZero(10.0), EPS);

    try {
      minMax.normalizeAroundZero(11.0);
      fail("IllegalArgumentException was expected");
    } catch (IllegalArgumentException iae) {
      // ok
    }
  }

  @Test
  public void test_normalizeAroundZero2() {
    MinMax minMax = MinMax.createFor(Arrays.asList(1.0, 2.0, 11.0).iterator());

    assertEquals(0.0, minMax.normalizeAroundZero(1.0), EPS);
    assertEquals(1.0, minMax.normalize(11.0), EPS);
    assertEquals(1.0, minMax.normalizeAroundZero(11.0), EPS);
    assertEquals(0.5, minMax.normalizeAroundZero(6.0), EPS);
  }

}
