package jflowmap.aggregation.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;

import jflowmap.data.MinMax;

import org.junit.Test;

public class MinMaxTest {

  private static final double EPS = 1e-7;

  @Test
  public void test_createFor_negative() {
    MinMax minMax = MinMax.createFor(Arrays.asList(-1.0, -1.1, -1.4, -100.0, -10.0).iterator());
    assertEquals(-100.0, minMax.getMin(), EPS);
    assertEquals(-1.0, minMax.getMax(), EPS);
  }

  @Test
  public void test_normalizeLog() {
    MinMax minMax = MinMax.createFor(Arrays.asList(1.0, 1.1, 1.4, 100.0, 10.0).iterator());
    assertEquals(0, minMax.normalizeLog(1), EPS);
    assertEquals(.5, minMax.normalizeLog(10), EPS);
    assertEquals(1.0, minMax.normalizeLog(100), EPS);
  }

  @Test
  public void test_normalizeLog_negative() {
    MinMax minMax = MinMax.createFor(Arrays.asList(-1.0, -1.1, -1.4, -100.0, -10.0).iterator());
    assertEquals(0, minMax.normalizeLog(-100), EPS);
    assertEquals(.5, minMax.normalizeLog(-90), 1e-1);
    assertEquals(1.0, minMax.normalizeLog(-1), EPS);
  }

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
    MinMax minMax = MinMax.createFor(Arrays.asList(1.0, 2.0, 10.0).iterator());

    assertEquals(0.1, minMax.normalizeAroundZero(1.0), EPS);
    assertEquals(0.5, minMax.normalizeAroundZero(5.0), EPS);
    assertEquals(1.0, minMax.normalizeAroundZero(10.0), EPS);


    MinMax minMax2 = MinMax.createFor(Arrays.asList(-1.0, -2.0, -10.0).iterator());
    assertEquals(-0.1, minMax2.normalizeAroundZero(-1.0), EPS);
    assertEquals(-0.5, minMax2.normalizeAroundZero(-5.0), EPS);
    assertEquals(-1.0, minMax2.normalizeAroundZero(-10.0), EPS);
  }

  @Test
  public void test_normalizeLogAroundZero() {
    MinMax minMax = MinMax.createFor(Arrays.asList(-22.0, -3.0, 1.0, 2.0, 100.0).iterator());
    assertEquals(0.0, minMax.normalizeLogAroundZero(0.0), EPS);
    assertEquals(-.5, minMax.normalizeLogAroundZero(-10.0), 1e-1);
    assertEquals(.5, minMax.normalizeLogAroundZero(10.0), 1e-1);
    assertEquals(1.0, minMax.normalizeLogAroundZero(100.0), EPS);
  }

}
