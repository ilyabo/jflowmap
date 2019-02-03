package jflowmap.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.Test;

public class SeqStatNormalizerTest {

  private static final double EPS = 1e-7;

  @Test
  public void test_normalizeLog_smallDiff() {
    SeqStat mm = SeqStat.createFor(Arrays.asList(7.47725554761535E-10, 1.0512433341615857));
    //mm.normalize(1.0512433341615857);
    assertEquals(1.0, mm.normalizer().normalizeLog(1.0512433341615857), EPS); // used to yield 1.0000000000000004
                                                                 // and throw an exception
  }

  @Test
  public void test_createFor_negative() {
    SeqStat minMax = SeqStat.createFor(Arrays.asList(-1.0, -1.1, -1.4, -100.0, -10.0).iterator());
    assertEquals(-100.0, minMax.getMin(), EPS);
    assertEquals(-1.0, minMax.getMax(), EPS);
  }

  @Test
  public void test_normalizeLog() {
    SeqStat minMax = SeqStat.createFor(Arrays.asList(1.0, 1.1, 1.4, 100.0, 10.0).iterator());
    assertEquals(0, minMax.normalizer().normalizeLog(1), EPS);
    assertEquals(.5, minMax.normalizer().normalizeLog(10), EPS);
    assertEquals(1.0, minMax.normalizer().normalizeLog(100), EPS);
  }

  @Test
  public void test_normalizeLog_negative() {
    SeqStat minMax = SeqStat.createFor(Arrays.asList(-1.0, -1.1, -1.4, -100.0, -10.0).iterator());
    assertEquals(0, minMax.normalizer().normalizeLog((-100)), EPS);
    assertEquals(.5, minMax.normalizer().normalizeLog((-90)), 1e-1);
    assertEquals(1.0, minMax.normalizer().normalizeLog((-1)), EPS);
  }

  @Test
  public void test_normalizeAroundZero() {
    SeqStat minMax = SeqStat.createFor(Arrays.asList(-5.0, 0.0, 0.1, 1.4, 10.0).iterator());

    assertEquals(-0.5, minMax.normalizer().normalizeAroundZero((-5)), EPS);
    assertEquals(-0.25, minMax.normalizer().normalizeAroundZero((-2.5)), EPS);
    assertEquals(0.0, minMax.normalizer().normalizeAroundZero(0.0), EPS);
    assertEquals(0.5, minMax.normalizer().normalizeAroundZero(5.0), EPS);
    assertEquals(1.0, minMax.normalizer().normalizeAroundZero(10.0), EPS);

    try {
      minMax.normalizer().normalizeAroundZero(11.0);
      fail("IllegalArgumentException was expected");
    } catch (IllegalArgumentException iae) {
      // ok
    }
  }


  @Test
  public void test_normalizeAroundZero2() {
    SeqStat minMax = SeqStat.createFor(Arrays.asList(1.0, 2.0, 10.0).iterator());

    assertEquals(0.1, minMax.normalizer().normalizeAroundZero(1.0), EPS);
    assertEquals(0.5, minMax.normalizer().normalizeAroundZero(5.0), EPS);
    assertEquals(1.0, minMax.normalizer().normalizeAroundZero(10.0), EPS);


    SeqStat minMax2 = SeqStat.createFor(Arrays.asList(-1.0, -2.0, -10.0).iterator());
    assertEquals(-0.1, minMax2.normalizer().normalizeAroundZero((-1.0)), EPS);
    assertEquals(-0.5, minMax2.normalizer().normalizeAroundZero((-5.0)), EPS);
    assertEquals(-1.0, minMax2.normalizer().normalizeAroundZero((-10.0)), EPS);
  }

  @Test
  public void test_normalizeLogAroundZero() {
    SeqStat minMax = SeqStat.createFor(Arrays.asList(-22.0, -3.0, 1.0, 2.0, 100.0).iterator());
    assertEquals(0.0, minMax.normalizer().normalizeLogAroundZero(0.0), EPS);
    assertEquals(-.5, minMax.normalizer().normalizeLogAroundZero((-10.0)), 1e-1);
    assertEquals(.5, minMax.normalizer().normalizeLogAroundZero(10.0), 1e-1);
    assertEquals(1.0, minMax.normalizer().normalizeLogAroundZero(100.0), EPS);
  }

}
