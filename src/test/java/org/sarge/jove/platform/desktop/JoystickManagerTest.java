package org.sarge.jove.platform.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.control.Button;
import org.sarge.jove.platform.desktop.DesktopLibraryJoystick.JoystickListener;
import org.sarge.jove.platform.desktop.JoystickManager.ConnectionListener;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public class JoystickManagerTest {
	private JoystickDevice dev;
	private DesktopLibrary lib;
	private JoystickManager manager;

	@BeforeEach
	void before() {
		// Init GLFW library
		lib = mock(DesktopLibrary.class);

		// Init joystick API methods
		final IntByReference count = new IntByReference() {
			@Override
			public boolean equals(Object obj) {
				return true;
			}
		};
		when(lib.glfwGetJoystickAxes(1, count)).thenReturn(new Pointer(0));
		when(lib.glfwGetJoystickButtons(1, count)).thenReturn(new Pointer(0));

		// Create a joystick
		dev = new JoystickDevice(1, "name", new JoystickAxis[]{}, new Button[]{}, lib);
		when(lib.glfwJoystickPresent(1)).thenReturn(true);
		when(lib.glfwGetJoystickName(1)).thenReturn("name");

		// Create manager
		final Desktop desktop = mock(Desktop.class);
		when(desktop.library()).thenReturn(lib);
		manager = new JoystickManager(desktop);
	}

	@Test
	void create() {
		for(int n = 0; n < 16; ++n) {
			verify(lib).glfwJoystickPresent(n);
		}
	}

	@Test
	void devices() {
		assertEquals(List.of(dev), manager.devices());
	}

	@Test
	void poll() {
		manager.poll();
	}

	@Test
	void connect() {
		// Register connection listener
		final var listener = mock(ConnectionListener.class);
		manager.listener(listener);

		// Check API
		final ArgumentCaptor<JoystickListener> captor = ArgumentCaptor.forClass(JoystickListener.class);
		verify(lib).glfwSetJoystickCallback(captor.capture());

		// Capture delegate listener
		final JoystickListener delegate = captor.getValue();
		assertNotNull(delegate);

		// Disconnect device
		delegate.connect(1, 0);
		assertEquals(List.of(), manager.devices());

		// Connect device
		delegate.connect(1, 0x00040001);
		assertEquals(List.of(dev), manager.devices());
	}
}
