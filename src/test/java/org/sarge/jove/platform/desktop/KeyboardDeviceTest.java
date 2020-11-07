package org.sarge.jove.platform.desktop;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.control.Button;
import org.sarge.jove.control.InputEvent;
import org.sarge.jove.platform.desktop.DesktopLibraryDevice.KeyListener;
import org.sarge.jove.platform.desktop.KeyboardDevice.KeyTable;

import com.sun.jna.Pointer;

public class KeyboardDeviceTest {
	private KeyboardDevice device;
	private Window window;
	private DesktopLibrary lib;
	private Consumer<InputEvent<Button>> handler;

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
		assertNotNull(device.sources());
		assertEquals(1, device.sources().size());
	}

	@Test
	void source() {
		final InputEvent.Source<?> src = device.sources().iterator().next();
		assertNotNull(src);
		assertEquals(List.of(), src.types());
	}

	@Test
	void enable() {
		// Enable button events
		device.enable(handler);

		// Check API
		final ArgumentCaptor<KeyListener> captor = ArgumentCaptor.forClass(KeyListener.class);
		verify(lib).glfwSetKeyCallback(eq(window.handle()), captor.capture());
		assertNotNull(captor.getValue());

		// Create button
		final int code = 256;
		final String name = KeyTable.INSTANCE.map(code);
		final Button button = new Button(name, 1, 2);

		// Generate an event
		final KeyListener listener = captor.getValue();
		listener.key(null, code, 0, 1, 2);

		// Check event delegated to handler
		verify(handler).accept(button);
	}
}
