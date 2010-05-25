package jflowmap.clustering;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


public class CosineTest {


  private static final double EPS = 1e-2;

  @Test
  public void testSimilarity() {
    assertEquals(1.0, Cosine.cosine(
        new double[] { 1, 2, 3 },
        new double[] { 1, 2, 3 })
    , EPS);
    assertEquals(0, Cosine.cosine(
        new double[] { 1, 0, 0 },
        new double[] { 0, 1, 0 })
    , EPS);
    assertEquals(-1, Cosine.cosine(
        new double[] { 1, 0, 0 },
        new double[] { -1, 0, 0 })
    , EPS);
    assertEquals(1.0, Cosine.cosine(
        new double[] { 100, 0, 0 },
        new double[] { 1, 0, 0 })
    , EPS);
    assertEquals(0, Cosine.cosine(new double[] { 100, 0, 0 }, new double[] { 0, 1, 0 }), EPS);

    assertTrue(Double.isNaN(Cosine.cosine(new double[] { 0, 0, 0 }, new double[] { 0, 0, 1 })));

    assertEquals(0.99, Cosine.cosine(new double[] { 100, 100, 100 }, new double[] { 100, 90, 100 }), EPS);
    assertEquals(0.85, Cosine.cosine(new double[] { 100, 100, 100 }, new double[] { 100, 10, 100 }), EPS);
    assertEquals(0.77, Cosine.cosine(new double[] { 0, 100, 100 }, new double[] { 0, 10, 100 }), EPS);


    assertTrue(
        Cosine.cosine(new double[] { 100, 100, 0 }, new double[] { 100, 100, 0 })  >
        Cosine.cosine(new double[] { 100, 100, 0 }, new double[] { 100, 1, 0 })
    );

  }


}
