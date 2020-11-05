package org.sarge.jove.platform.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.control.Axis;
import org.sarge.jove.control.Button;
import org.sarge.jove.control.Button.Operation;
import org.sarge.jove.control.InputEvent;
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
		device = new MouseDevice(window);

		// Create handler
		handler = mock(Consumer.class);
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
		final Source<?> pointer = device.pointer();
		assertNotNull(pointer);
		assertEquals(Position.class, pointer.type());
		assertNotNull(pointer.events());
		assertEquals(1, pointer.events().size());

		// Enable mouse pointer
		final ArgumentCaptor<MousePositionListener> captor = ArgumentCaptor.forClass(MousePositionListener.class);
		pointer.enable(handler);
		verify(lib).glfwSetCursorPosCallback(eq(window.handle()), captor.capture());

		// Generate an event
		final MousePositionListener listener = captor.getValue();
		assertNotNull(listener);
		listener.move(null, 1, 2);
		verify(handler).accept(new Position.Event(1, 2));
	}

	@Test
	void buttons() {
		// Retrieve mouse buttons source
		final Source<?> buttons = device.buttons();
		assertNotNull(buttons);
		assertEquals(Button.class, buttons.type());
		assertNotNull(buttons.events());

		// Enable mouse buttons
		final ArgumentCaptor<MouseButtonListener> captor = ArgumentCaptor.forClass(MouseButtonListener.class);
		buttons.enable(handler);
		verify(lib).glfwSetMouseButtonCallback(eq(window.handle()), captor.capture());

		// Lookup axis
		final Button button = (Button) buttons.events().get(0);
		assertNotNull(button);
		assertEquals("Button-0", button.name());

		// Generate an event
		final MouseButtonListener listener = captor.getValue();
		assertNotNull(listener);
		listener.button(null, 0, 0, 0);
		verify(handler).accept(button.event(Operation.PRESS));
	}

	@Test
	void wheel() {
		// Retrieve mouse wheel source
		final Source<?> wheel = device.wheel();
		assertNotNull(wheel);
		assertEquals(Axis.class, wheel.type());
		assertNotNull(wheel.events());
		assertEquals(1, wheel.events().size());

		// Lookup axis
		final Axis axis = (Axis) wheel.events().get(0);
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
