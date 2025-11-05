package org.sarge.jove.platform.desktop;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.platform.desktop.JoystickLibrary.JoystickListener;
import org.sarge.jove.platform.desktop.JoystickManager.ConnectionListener;

@Disabled
class JoystickManagerTest extends AbstractJoystickTest {
	private JoystickDevice dev;
	private JoystickManager manager;

	@BeforeEach
	void before() {
		// Create a joystick
		dev = new JoystickDevice(1, "name", desktop);
		when(lib.glfwJoystickPresent(1)).thenReturn(true);
		when(lib.glfwGetJoystickName(1)).thenReturn("name");

		// Create manager
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
