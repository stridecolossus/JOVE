package org.sarge.jove.platform.desktop;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.control.Button;
import org.sarge.jove.control.InputEvent;
import org.sarge.jove.platform.desktop.DesktopLibraryDevice.KeyListener;

import com.sun.jna.Pointer;

public class KeyboardDeviceTest {
	private KeyboardDevice device;
	private Window window;
	private DesktopLibrary lib;
	private InputEvent.Handler handler;

	@BeforeEach
	void before() {
		// Create API
		lib = mock(DesktopLibrary.class);

		// Create window
		window = mock(Window.class);
		when(window.library()).thenReturn(lib);
		when(window.handle()).thenReturn(new Handle(new Pointer(1)));

		// Create device
		device = new KeyboardDevice(window);

		// Create handler
		handler = mock(InputEvent.Handler.class);
	}

	@Test
	void constructor() {
		assertEquals("Keyboard", device.name());
		assertEquals(Set.of(Button.class), device.types());
	}

	@Test
	void enable() {
		// Enable button events
		device.enable(Button.class, handler);

		// Check API
		final ArgumentCaptor<KeyListener> captor = ArgumentCaptor.forClass(KeyListener.class);
		verify(lib).glfwSetKeyCallback(eq(window.handle()), captor.capture());
		assertNotNull(captor.getValue());

		// Generate an event
		final KeyListener listener = captor.getValue();
		listener.key(null, 1, 2, 3, 4);

		// Check event delegated to handler
		final Button button = new Button(1, 3, 4);
		verify(handler).handle(button.event());
	}

	@Test
	void enableInvalidEventType() {
		assertThrows(IllegalArgumentException.class, () -> device.enable(InputEvent.Type.Position.class, handler));
	}

	@Test
	void disable() {
		device.disable(Button.class);
		verify(lib).glfwSetKeyCallback(window.handle(), null);
	}
}
