package org.sarge.jove.platform.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.control.InputEvent;
import org.sarge.jove.control.InputEvent.Type;
import org.sarge.jove.control.InputEvent.Type.Axis;
import org.sarge.jove.control.InputEvent.Type.Button;
import org.sarge.jove.control.InputEvent.Type.Position;
import org.sarge.jove.platform.desktop.DesktopLibraryDevice.MouseButtonListener;
import org.sarge.jove.platform.desktop.DesktopLibraryDevice.MousePositionListener;
import org.sarge.jove.platform.desktop.DesktopLibraryDevice.MouseScrollListener;

import com.sun.jna.Pointer;

public class MouseDeviceTest {
	private MouseDevice device;
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
		device = new MouseDevice(window);

		// Create handler
		handler = mock(InputEvent.Handler.class);
	}

	@Test
	void constructor() {
		assertEquals("Mouse", device.name());
		assertEquals(Set.of(Button.class, Position.class, Axis.class), device.types());
	}

	@Test
	void enable() {
		device.enable(Button.class, handler);
		device.enable(Position.class, handler);
		device.enable(Axis.class, handler);
		verify(lib).glfwSetMouseButtonCallback(eq(window.handle()), isA(MouseButtonListener.class));
		verify(lib).glfwSetCursorPosCallback(eq(window.handle()), isA(MousePositionListener.class));
		verify(lib).glfwSetScrollCallback(eq(window.handle()), isA(MouseScrollListener.class));
	}

	@Test
	void enableInvalidEventType() {
		class Mock implements InputEvent.Type {
			// Empty implementation
		}
		assertThrows(IllegalArgumentException.class, () -> device.enable(Mock.class, handler));
	}

	@Test
	void disable() {
		device.disable(Button.class);
		device.disable(Position.class);
		device.disable(Axis.class);
		verify(lib).glfwSetMouseButtonCallback(window.handle(), null);
		verify(lib).glfwSetCursorPosCallback(window.handle(), null);
		verify(lib).glfwSetScrollCallback(window.handle(), null);
	}

	@Test
	void position() {
		final MousePositionListener listener = device.position(handler);
		listener.move(null, 1, 2);
		verify(handler).handle(Type.POSITION.create(1, 2));
	}

	@Test
	void button() {
		final Button button = new Button(1, "1", 2, 3);
		final MouseButtonListener listener = device.button(handler);
		listener.button(null, 1, 2, 3);
		verify(handler).handle(button.event());
	}

	@Test
	void wheel() {
		final Axis axis = new Axis(0, "Wheel");
		final MouseScrollListener listener = device.wheel(handler);
		listener.scroll(null, 1, 2);
		verify(handler).handle(axis.create(2));
	}
}
