package jflowmap.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MathUtilsTest {

  private static final double EPS = 1e-7;

  @Test
  public void testLog() {
    assertEquals(2, MathUtils.log(4, 2), EPS);
    assertEquals(6, MathUtils.log(64, 2), EPS);
    assertEquals(Math.log(2), MathUtils.log(2, Math.E), EPS);
    assertEquals(Math.log(16), MathUtils.log(16, Math.E), EPS);
    assertEquals(3, MathUtils.log(1000, 10), EPS);
  }

  @Test
  public void testCompareDoubles_smallestIsNaN() {
    assertEquals(1, MathUtils.compareDoubles_smallestIsNaN(100.0, 50.0));
    assertEquals(0, MathUtils.compareDoubles_smallestIsNaN(Double.NaN, Double.NaN));
    assertEquals(1, MathUtils.compareDoubles_smallestIsNaN(100.0, Double.NaN));
    assertEquals(1, MathUtils.compareDoubles_smallestIsNaN(-100.0, Double.NaN));
    assertEquals(-1, MathUtils.compareDoubles_smallestIsNaN(Double.NaN, 100.0));
    assertEquals(1, MathUtils.compareDoubles_smallestIsNaN(Double.POSITIVE_INFINITY, Double.NaN));
    assertEquals(1, MathUtils.compareDoubles_smallestIsNaN(Double.NEGATIVE_INFINITY, Double.NaN));
  }

}
