package org.sarge.jove.platform.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.control.AxisEvent;
import org.sarge.jove.control.ButtonEvent;
import org.sarge.jove.control.Event;
import org.sarge.jove.control.Event.Type;
import org.sarge.jove.control.PositionEvent;
import org.sarge.jove.platform.desktop.DesktopDevice.DesktopSource;
import org.sarge.jove.platform.desktop.DesktopLibraryDevice.MouseButtonListener;
import org.sarge.jove.platform.desktop.DesktopLibraryDevice.MousePositionListener;
import org.sarge.jove.platform.desktop.DesktopLibraryDevice.MouseScrollListener;

public class MouseDeviceTest {
	private MouseDevice mouse;
	private Window window;
	private DesktopLibrary lib;
	private Consumer<Event> handler;

	@BeforeEach
	void before() {
		window = mock(Window.class);
		lib = mock(DesktopLibrary.class);
		handler = mock(Consumer.class);
		mouse = new MouseDevice(window);
	}

	@Test
	void constructor() {
		assertNotNull(mouse.sources());
	}

	@Nested
	class MousePointerTests {
		private DesktopSource<MousePositionListener> ptr;

		@BeforeEach
		void before() {
			ptr = mouse.pointer();
		}

		@Test
		void constructor() {
			assertNotNull(ptr);
			assertEquals(mouse, ptr.device());
			assertTrue(mouse.sources().contains(ptr));
			assertNotNull(ptr.method(lib));
		}

		@Test
		void types() {
			final Collection<Type> types = ptr.types();
			assertNotNull(types);
			assertEquals(1, types.size());
			assertEquals(new Type("Pointer"), types.iterator().next());
		}

		@Test
		void listener() {
			final MousePositionListener listener = ptr.listener(handler);
			assertNotNull(listener);
			listener.move(null, 1, 2);
			verify(handler).accept(new PositionEvent(new Type("Pointer"), ptr, 1, 2));
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
		private DesktopSource<MouseButtonListener> buttons;

		@BeforeEach
		void before() {
			buttons = mouse.buttons();
		}

		@Test
		void constructor() {
			assertNotNull(buttons);
			assertEquals(mouse, buttons.device());
			assertTrue(mouse.sources().contains(buttons));
			assertNotNull(buttons.method(lib));
		}

		@Test
		void types() {
			final Collection<Type> types = buttons.types();
			assertNotNull(types);
			assertEquals(new Type("Mouse-Button-1"), types.iterator().next());
		}

		@Test
		void listener() {
			final Type one = buttons.types().iterator().next();
			final MouseButtonListener listener = buttons.listener(handler);
			assertNotNull(listener);
			listener.button(null, 0, 1, 0x0002);
			verify(handler).accept(new ButtonEvent("Mouse-Button-1-PRESS-CONTROL", one, buttons));
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
		private DesktopSource<MouseScrollListener> wheel;

		@BeforeEach
		void before() {
			wheel = mouse.wheel();
		}

		@Test
		void constructor() {
			assertNotNull(wheel);
			assertEquals(mouse, wheel.device());
			assertTrue(mouse.sources().contains(wheel));
			assertNotNull(wheel.method(lib));
		}

		@Test
		void types() {
			final Collection<Type> types = wheel.types();
			assertNotNull(types);
			assertEquals(1, types.size());
			assertEquals(new Type("Wheel"), types.iterator().next());
		}

		@Test
		void listener() {
			final MouseScrollListener listener = wheel.listener(handler);
			assertNotNull(listener);
			listener.scroll(null, 1, 2);
			verify(handler).accept(new AxisEvent(new Type("Wheel"), wheel, 2));
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
