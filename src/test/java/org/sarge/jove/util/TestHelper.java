package org.sarge.jove.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit-test helper utilities.
 * @author Sarge
 */
public class TestHelper {
	private TestHelper() {
	}

	/**
	 * Asserts that the given floating-point values are approximately equal.
	 * @param expected		Expected value
	 * @param actual		Actual value
	 */
	public static void assertFloatEquals(float expected, float actual) {
		assertEquals(expected, actual, MathsUtil.ACCURACY);
	}

	/**
	 * Asserts that the given floating-point arrays are approximately equal.
	 * @param expected		Expected value
	 * @param actual		Actual value
	 */
	public static void assertFloatArrayEquals(float[] expected, float[] actual) {
		assertArrayEquals(expected, actual, MathsUtil.ACCURACY);
	}
}
