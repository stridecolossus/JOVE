package org.sarge.jove.platform.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.control.Axis;
import org.sarge.jove.control.Button;
import org.sarge.jove.control.InputEvent.Handler;
import org.sarge.jove.control.InputEvent.Source;
import org.sarge.jove.control.Position;
import org.sarge.jove.platform.desktop.DesktopLibraryDevice.MouseButtonListener;
import org.sarge.jove.platform.desktop.DesktopLibraryDevice.MousePositionListener;
import org.sarge.jove.platform.desktop.DesktopLibraryDevice.MouseScrollListener;

import com.sun.jna.Pointer;

public class MouseDeviceTest {
	private MouseDevice device;
	private Window window;
	private DesktopLibrary lib;
	private Handler handler;

	@BeforeEach
	void before() {
		// Create API
		lib = mock(DesktopLibrary.class);

		// Create window
		window = mock(Window.class);
		when(window.library()).thenReturn(lib);
		when(window.handle()).thenReturn(new Handle(new Pointer(1)));

		// Create device
		device = new MouseDevice(window);

		// Create handler
		handler = mock(Handler.class);
	}

	@Test
	void constructor() {
		assertEquals("Mouse", device.name());
		assertNotNull(device.sources());
		assertEquals(3, device.sources().size());
	}

	@Test
	void pointer() {
		// Retrieve mouse pointer source
		final Source<Position.Event> pointer = device.pointer();
		assertNotNull(pointer);
		assertNotNull(pointer.types());
		assertEquals(1, pointer.types().size());

		// Enable mouse pointer
		final Handler handler = mock(Handler.class);
		final ArgumentCaptor<MousePositionListener> captor = ArgumentCaptor.forClass(MousePositionListener.class);
		pointer.enable(handler);
		verify(lib).glfwSetCursorPosCallback(eq(window.handle()), captor.capture());

		// Generate an event
		final Position pos = (Position) pointer.types().get(0);
		final MousePositionListener listener = captor.getValue();
		assertNotNull(listener);
		listener.move(null, 1, 2);
		verify(handler).accept(new Position.Event(pos, 1, 2));
	}

	@Test
	void buttons() {
		// Retrieve mouse buttons source
		final Source<Button> buttons = device.buttons();
		assertNotNull(buttons);
		assertNotNull(buttons.types());

		// Enable mouse buttons
		final Handler handler = mock(Handler.class);
		final ArgumentCaptor<MouseButtonListener> captor = ArgumentCaptor.forClass(MouseButtonListener.class);
		buttons.enable(handler);
		verify(lib).glfwSetMouseButtonCallback(eq(window.handle()), captor.capture());

		// Lookup axis
		final Button button = (Button) buttons.types().get(0);
		assertNotNull(button);
		assertEquals("Button-1-PRESS", button.name());

		// Generate an event
		final MouseButtonListener listener = captor.getValue();
		assertNotNull(listener);
		listener.button(null, 0, 0, 0);
		verify(handler).accept(button);
	}

	@Test
	void wheel() {
		// Retrieve mouse wheel source
		final Source<Axis.Event> wheel = device.wheel();
		assertNotNull(wheel);
		assertNotNull(wheel.types());
		assertEquals(1, wheel.types().size());

		// Lookup axis
		final Axis axis = (Axis) wheel.types().get(0);
		assertNotNull(axis);
		assertEquals("Wheel", axis.name());

		// Enable mouse wheel
		final ArgumentCaptor<MouseScrollListener> captor = ArgumentCaptor.forClass(MouseScrollListener.class);
		wheel.enable(handler);
		verify(lib).glfwSetScrollCallback(eq(window.handle()), captor.capture());

		// Generate an event
		final MouseScrollListener listener = captor.getValue();
		assertNotNull(listener);
		listener.scroll(null, 1, 2);
		verify(handler).accept(axis.create(2));
	}
}
