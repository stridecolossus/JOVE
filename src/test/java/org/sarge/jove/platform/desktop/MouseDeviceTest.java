package org.sarge.jove.platform.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.control.Axis;
import org.sarge.jove.control.Button;
import org.sarge.jove.control.Button.Action;
import org.sarge.jove.control.Event;
import org.sarge.jove.control.Position;
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
		private DesktopSource<MousePositionListener> src;
		private Position ptr;

		@BeforeEach
		void before() {
			ptr = mouse.pointer();
			src = (DesktopSource<MousePositionListener>) ptr.source();
		}

		@Test
		void constructor() {
			assertNotNull(ptr);
			assertNotNull("Pointer", ptr.name());
		}

		@Test
		void source() {
			assertNotNull(src);
			assertEquals(mouse, src.device());
			assertTrue(mouse.sources().contains(src));
			assertEquals(List.of(ptr), src.types());
		}

		@Test
		void listener() {
			final MousePositionListener listener = src.listener(handler);
			assertNotNull(listener);
			listener.move(null, 1, 2);
			verify(handler).accept(ptr.new PositionEvent(1, 2));
		}

		@Test
		void method() {
			final MousePositionListener listener = mock(MousePositionListener.class);
			final BiConsumer<Window, MousePositionListener> method = src.method(lib);
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
			buttons = (DesktopSource<MouseButtonListener>) mouse.buttons();
		}

		@Test
		void constructor() {
			assertNotNull(buttons);
			assertEquals(mouse, buttons.device());
			assertTrue(mouse.sources().contains(buttons));
		}

		@Test
		void types() {
			final var types = buttons.types();
			assertNotNull(types);
			assertEquals(new Button("Mouse-1", buttons), types.get(0));
		}

		@Test
		void listener() {
			final MouseButtonListener listener = buttons.listener(handler);
			assertNotNull(listener);
			listener.button(null, 0, 1, 0x0002);
			verify(handler).accept(new Button("Mouse-1", buttons, Action.PRESS, 0x0002));
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
		private DesktopSource<MouseScrollListener> src;
		private Axis wheel;

		@BeforeEach
		void before() {
			wheel = mouse.wheel();
			src = (DesktopSource<MouseScrollListener>) wheel.source();
		}

		@Test
		void constructor() {
			assertNotNull(wheel);
			assertEquals("Wheel", wheel.name());
		}

		@Test
		void source() {
			assertNotNull(src);
			assertEquals(mouse, src.device());
			assertTrue(mouse.sources().contains(src));
			assertNotNull(src.method(lib));
			assertEquals(List.of(wheel), src.types());
		}

		@Test
		void listener() {
			final MouseScrollListener listener = src.listener(handler);
			assertNotNull(listener);
			listener.scroll(null, 1, 2);
			verify(handler).accept(wheel.new AxisEvent(2));
		}

		@Test
		void method() {
			final MouseScrollListener listener = mock(MouseScrollListener.class);
			final BiConsumer<Window, MouseScrollListener> method = src.method(lib);
			assertNotNull(method);
			method.accept(window, listener);
			verify(lib).glfwSetScrollCallback(window, listener);
		}
	}
}
