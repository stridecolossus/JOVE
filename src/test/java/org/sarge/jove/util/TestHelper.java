package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;

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

	/**
	 * Asserts that the given code throws an exception containing the expected message.
	 * @param clazz			Exception class
	 * @param message		Expected message
	 * @param code			Code
	 */
	public static void assertThrows(Class<? extends Exception> clazz, String message, Executable code) {
		final Exception e = Assertions.assertThrows(clazz, code);
		assertTrue(e.getMessage().contains(message), () -> String.format("Expected exception message: expected=[%s] actual=[%s]", message, e.getMessage()));
	}
}
