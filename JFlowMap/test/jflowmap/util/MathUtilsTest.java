package jflowmap.util;

import static org.junit.Assert.*;

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

}
