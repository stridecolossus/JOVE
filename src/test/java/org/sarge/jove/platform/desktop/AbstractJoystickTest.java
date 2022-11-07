package org.sarge.jove.platform.desktop;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.stubbing.Answer;
import org.sarge.jove.util.ReferenceFactory;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

/**
 * Base-class for joystick tests.
 * @author Sarge
 */
abstract class AbstractJoystickTest {
	protected Desktop desktop;
	protected DesktopLibrary lib;
	protected ReferenceFactory factory;
	protected boolean pressed;

	@BeforeEach
	void init() {
		// Init GLFW
		desktop = mock(Desktop.class);
		lib = mock(DesktopLibrary.class);
		when(desktop.library()).thenReturn(lib);

		// Init reference factory
		factory = mock(ReferenceFactory.class);
		when(desktop.factory()).thenReturn(factory);

		// Mock array count
		final IntByReference count = new IntByReference(1) {
			@Override
			public boolean equals(Object obj) {
				return true;
			}
		};
		when(factory.integer()).thenReturn(count);

		// Init axis values
		final Pointer axes = new Memory(Float.BYTES);
		axes.setFloat(0, 0);
		when(lib.glfwGetJoystickAxes(1, count)).thenReturn(axes);

		// Init buttons
		final Answer<Pointer> buttons = inv -> {
			final Pointer ptr = new Memory(1);
			ptr.setByte(0, (byte) (pressed ? 1 : 0));
			return ptr;
		};
		when(lib.glfwGetJoystickButtons(1, count)).then(buttons);

		// Init hats
		final Answer<Pointer> hats = inv -> {
			final Pointer ptr = new Memory(1);
			ptr.setByte(0, (byte) (pressed ? (1 | 2) : 0));
			return ptr;
		};
		when(lib.glfwGetJoystickHats(1, count)).then(hats);
	}
}
