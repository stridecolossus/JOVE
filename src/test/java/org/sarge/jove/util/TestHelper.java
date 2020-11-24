package org.sarge.jove.util;

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
	 * Asserts that the given code throws an exception containing the expected message.
	 * @param clazz			Exception class
	 * @param message		Expected message
	 * @param code			Code
	 */
	public static void assertThrows(Class<? extends Exception> clazz, String message, Executable code) {
		Check.notEmpty(message);
		final Exception e = Assertions.assertThrows(clazz, code);
		assertTrue(e.getMessage().contains(message), () -> String.format("Expected exception message: expected=[%s] actual=[%s]", message, e.getMessage()));
	}
}
