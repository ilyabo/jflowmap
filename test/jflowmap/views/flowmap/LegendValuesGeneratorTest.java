package jflowmap.views.flowmap;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;


public class LegendValuesGeneratorTest {

  @Test
  public void test_smallerThanOne() {
    assertEquals(Arrays.<Double>asList(100.,50.,10.,1.), LegendValuesGenerator.generate(1, 100, 4));
    assertEquals(Arrays.<Double>asList(1.,.5,.1), LegendValuesGenerator.generate(.1, 1., 4));
    // TODO: values <0 don't work
  }
}
