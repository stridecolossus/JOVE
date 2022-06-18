package org.sarge.jove.platform.desktop;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Set;
import java.util.function.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.control.*;
import org.sarge.jove.platform.desktop.DesktopDevice.DesktopSource;
import org.sarge.jove.platform.desktop.DesktopLibraryDevice.*;
import org.sarge.jove.util.IntegerEnumeration;

@SuppressWarnings("unchecked")
public class MouseDeviceTest {
	private MouseDevice mouse;
	private Window window;
	private DesktopLibrary lib;
	private Consumer<PositionEvent> handler;

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

//	@Test
//	void count() {
//		assertEquals(true, mouse.count() > 0);
//	}

	@Nested
	class MousePointerTests {
		private DesktopSource<MouseListener, PositionEvent> ptr;

		@BeforeEach
		void before() {
			ptr = (DesktopSource<MouseListener, PositionEvent>) mouse.pointer();
		}

		@Test
		void constructor() {
			assertNotNull(ptr);
		}

		@Test
		void listener() {
			final MouseListener listener = ptr.listener(handler);
			assertNotNull(listener);
			listener.event(null, 1, 2);
			verify(handler).accept(new PositionEvent(ptr, 1, 2));
		}

		@Test
		void method() {
			final MouseListener listener = mock(MouseListener.class);
			final BiConsumer<Window, MouseListener> method = ptr.method(lib);
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
			verify(handler).accept(new ModifiedButton("Mouse-1").resolve(1, mods));
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
		private DesktopSource<MouseListener, AxisEvent> wheel;

		@BeforeEach
		void before() {
			wheel = (DesktopSource<MouseListener, AxisEvent>) mouse.wheel();
		}

		@Test
		void constructor() {
			assertNotNull(wheel);
		}

		@Test
		void listener() {
			final MouseListener listener = wheel.listener(handler);
			assertNotNull(listener);
			listener.event(null, 1, 2);
			verify(handler).accept(new AxisEvent((Axis) wheel, 2));
		}

		@Test
		void method() {
			final MouseListener listener = mock(MouseListener.class);
			final BiConsumer<Window, MouseListener> method = wheel.method(lib);
			assertNotNull(method);
			method.accept(window, listener);
			verify(lib).glfwSetScrollCallback(window, listener);
		}
	}
}
