package org.sarge.jove.platform.desktop;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.stubbing.Answer;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

/**
 * Base-class for joystick tests.
 * @author Sarge
 */
abstract class AbstractJoystickTest {
	protected DesktopLibrary lib;
	protected boolean pressed;

	@BeforeEach
	void init() {
		// Init GLFW library
		lib = mock(DesktopLibrary.class);

		// Mock array count
		final IntByReference ref = new IntByReference(1) {
			@Override
			public boolean equals(Object obj) {
				return true;
			}
		};

		// Init axis values
		final Answer<Pointer> axes = inv -> {
			final IntByReference count = inv.getArgument(1);
			final Pointer ptr = new Memory(Float.BYTES);
			ptr.setFloat(0, 0);
			count.setValue(1);
			return ptr;
		};
		when(lib.glfwGetJoystickAxes(1, ref)).then(axes);

		// Init buttons
		final Answer<Pointer> buttons = inv -> {
			final IntByReference count = inv.getArgument(1);
			final Pointer ptr = new Memory(1);
			ptr.setByte(0, (byte) (pressed ? 1 : 0));
			count.setValue(1);
			return ptr;
		};
		when(lib.glfwGetJoystickButtons(1, ref)).then(buttons);

		// Init hats
		final Answer<Pointer> hats = inv -> {
			final IntByReference count = inv.getArgument(1);
			final Pointer ptr = new Memory(1);
			ptr.setByte(0, (byte) (pressed ? (1 | 2) : 0));
			count.setValue(1);
			return ptr;
		};
		when(lib.glfwGetJoystickHats(1, ref)).then(hats);
	}
}
