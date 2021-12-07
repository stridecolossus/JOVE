package org.sarge.jove.platform.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.control.Axis;
import org.sarge.jove.control.Axis.AxisEvent;
import org.sarge.jove.control.Button;
import org.sarge.jove.control.DefaultButton;
import org.sarge.jove.control.DefaultButton.Modifier;
import org.sarge.jove.control.Event;
import org.sarge.jove.control.PositionEvent;
import org.sarge.jove.platform.desktop.DesktopDevice.DesktopSource;
import org.sarge.jove.platform.desktop.DesktopLibraryDevice.MouseButtonListener;
import org.sarge.jove.platform.desktop.DesktopLibraryDevice.MousePositionListener;
import org.sarge.jove.platform.desktop.DesktopLibraryDevice.MouseScrollListener;
import org.sarge.jove.util.IntegerEnumeration;

public class MouseDeviceTest {
	private MouseDevice mouse;
	private Window window;
	private DesktopLibrary lib;
	private Consumer<Event> handler;

	@BeforeEach
	void before() {
		window = mock(Window.class);
		lib = mock(DesktopLibrary.class);
		mouse = new MouseDevice(window);
		handler = mock(Consumer.class);
	}

	@Test
	void constructor() {
		assertEquals(Set.of(mouse.pointer(), mouse.buttons(), mouse.wheel()), mouse.sources());
	}

	@Test
	void count() {
		assertEquals(true, mouse.count() > 0);
	}

	@Nested
	class MousePointerTests {
		private DesktopSource<MousePositionListener, PositionEvent> ptr;

		@BeforeEach
		void before() {
			ptr = (DesktopSource<MousePositionListener, PositionEvent>) mouse.pointer();
		}

		@Test
		void constructor() {
			assertNotNull(ptr);
		}

		@Test
		void listener() {
			final MousePositionListener listener = ptr.listener(handler);
			assertNotNull(listener);
			listener.move(null, 1, 2);
			verify(handler).accept(new PositionEvent(ptr, 1, 2));
		}

		@Test
		void method() {
			final MousePositionListener listener = mock(MousePositionListener.class);
			final BiConsumer<Window, MousePositionListener> method = ptr.method(lib);
			assertNotNull(method);
			method.accept(window, listener);
			verify(lib).glfwSetCursorPosCallback(window, listener);
		}
	}

	@Nested
	class MouseButtonTests {
		private DesktopSource<MouseButtonListener, Button> buttons;

		@BeforeEach
		void before() {
			buttons = (DesktopSource<MouseButtonListener, Button>) mouse.buttons();
		}

		@Test
		void constructor() {
			assertNotNull(buttons);
			assertTrue(mouse.sources().contains(buttons));
		}

		@Test
		void listener() {
			final int mods = IntegerEnumeration.mask(Modifier.CONTROL);
			final MouseButtonListener listener = buttons.listener(handler);
			assertNotNull(listener);
			listener.button(null, 0, 1, mods);
			verify(handler).accept(new DefaultButton("Mouse-1").resolve(1, mods));
		}

		@Test
		void method() {
			final MouseButtonListener listener = mock(MouseButtonListener.class);
			final BiConsumer<Window, MouseButtonListener> method = buttons.method(lib);
			assertNotNull(method);
			method.accept(window, listener);
			verify(lib).glfwSetMouseButtonCallback(window, listener);
		}
	}

	@Nested
	class MouseWheelTests {
		private DesktopSource<MouseScrollListener, AxisEvent> wheel;

		@BeforeEach
		void before() {
			wheel = (DesktopSource<MouseScrollListener, AxisEvent>) mouse.wheel();
		}

		@Test
		void constructor() {
			assertNotNull(wheel);
		}

		@Test
		void listener() {
			final MouseScrollListener listener = wheel.listener(handler);
			assertNotNull(listener);
			listener.scroll(null, 1, 2);
			verify(handler).accept(new AxisEvent((Axis) wheel, 2));
		}

		@Test
		void method() {
			final MouseScrollListener listener = mock(MouseScrollListener.class);
			final BiConsumer<Window, MouseScrollListener> method = wheel.method(lib);
			assertNotNull(method);
			method.accept(window, listener);
			verify(lib).glfwSetScrollCallback(window, listener);
		}
	}
}
