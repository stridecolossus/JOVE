package org.sarge.jove.platform.desktop;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.control.Button;
import org.sarge.jove.control.Button.Operation;
import org.sarge.jove.control.InputEvent;
import org.sarge.jove.control.Position;
import org.sarge.jove.platform.desktop.DesktopLibraryDevice.KeyListener;

import com.sun.jna.Pointer;

public class KeyboardDeviceTest {
	private KeyboardDevice device;
	private Window window;
	private DesktopLibrary lib;
	private Consumer<InputEvent<?>> handler;

	@SuppressWarnings("unchecked")
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
		handler = mock(Consumer.class);
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

		// Create ENTER button
		final int code = 256;
		final Button button = device.key(code);

		// Generate an event
		final KeyListener listener = captor.getValue();
		listener.key(null, code, 2, 3, 4);

		// Check event delegated to handler
		verify(handler).accept(button.event(Operation.PRESS));
	}

	@Test
	void enableInvalidEventType() {
		assertThrows(IllegalArgumentException.class, () -> device.enable(Position.PositionType.class, handler));
	}

//	@Test
//	void disable() {
//		device.disable(Button.class);
//		verify(lib).glfwSetKeyCallback(window.handle(), null);
//	}

	@Test
	void key() {
		final Button enter = device.key(256);
		assertNotNull(enter);
		assertEquals(enter, device.key("ESCAPE"));
	}
}
