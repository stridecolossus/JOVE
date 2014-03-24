package org.sarge.jove.util;

/**
 * Test utilities.
 * @author Sarge
 */
public final class TestHelper {
	private TestHelper() {
		// Utility class
	}
	
	/**
	 * Asserts the floating-point value is as expected.
	 * @see MathsUtil#EPSILON
	 * @param expected		Expected value
	 * @param value			Actual value
	 */
	public static void assertFloatEquals( float expected, float value ) {
		org.junit.Assert.assertEquals( expected, value, MathsUtil.EPSILON );
	}
}
