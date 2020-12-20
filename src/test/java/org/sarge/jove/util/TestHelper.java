package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.sarge.jove.util.Check.notNull;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentMatcher;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * Unit-test helper utilities.
 * @author Sarge
 */
public final class TestHelper {
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

	/**
	 * Mockito argument matcher for a JNA integer-by-reference argument.
	 */
	public static class IntByReferenceMatcher implements ArgumentMatcher<IntByReference> {
		private final int value;

		/**
		 * Constructor.
		 * @param value Returned integer value
		 */
		public IntByReferenceMatcher(int value) {
			this.value = value;
		}

		@Override
		public boolean matches(IntByReference ref) {
			ref.setValue(value);
			return true;
		}
	}

	/**
	 * Mockito argument matcher for a JNA pointer-by-reference argument.
	 */
	public static class PointerByReferenceMatcher implements ArgumentMatcher<PointerByReference> {
		private final Pointer ptr;

		public PointerByReferenceMatcher() {
			this(new Pointer(1));
		}

		/**
		 * Constructor.
		 * @param ptr Returned pointer
		 */
		public PointerByReferenceMatcher(Pointer ptr) {
			this.ptr = notNull(ptr);
		}

		public Pointer value() {
			return ptr;
		}

		@Override
		public boolean matches(PointerByReference ref) {
			ref.setValue(ptr);
			return true;
		}
	}
}
