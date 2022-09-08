package org.sarge.jove.platform.desktop;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Set;
import java.util.function.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.control.*;
import org.sarge.jove.control.Button.Action;
import org.sarge.jove.platform.desktop.DesktopLibraryDevice.*;

@SuppressWarnings("unchecked")
public class MouseDeviceTest {
	private MouseDevice mouse;
	private Window window;
	private DesktopLibrary lib;

	@BeforeEach
	void before() {
		window = mock(Window.class);
		lib = mock(DesktopLibrary.class);
		mouse = new MouseDevice(window);
	}

	@Test
	void constructor() {
		assertEquals(Set.of(mouse.pointer(), mouse.buttons(), mouse.wheel()), mouse.sources());
	}

	@Nested
	class MousePointerTests {
		private DesktopSource<MouseListener, Position> ptr;

		@BeforeEach
		void before() {
			ptr = (DesktopSource<MouseListener, Position>) mouse.pointer();
		}

		@Test
		void constructor() {
			assertNotNull(ptr);
		}

		// TODO
//		@Test
//		void listener() {
//			final MouseListener listener = ptr.listener(handler);
//			assertNotNull(listener);
//			listener.event(null, 1, 2);
//			verify(handler).accept(new PositionEvent(ptr, 1, 2));
//		}

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
		private DesktopSource<MouseButtonListener, Button<Action>> buttons;

		@BeforeEach
		void before() {
			buttons = (DesktopSource<MouseButtonListener, Button<Action>>) mouse.buttons();
		}

		@Test
		void constructor() {
			assertNotNull(buttons);
			assertTrue(mouse.sources().contains(buttons));
		}

// TODO
//		@Test
//		void listener() {
//			final int mods = IntegerEnumeration.mask(Modifier.CONTROL);
//			final MouseButtonListener listener = buttons.listener(handler);
//			assertNotNull(listener);
//			listener.button(null, 0, 1, mods);
//			verify(handler).accept(new ModifiedButton("Mouse-1").resolve(1, mods));
//		}

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
		private DesktopSource<MouseListener, AxisControl> wheel;

		@BeforeEach
		void before() {
			wheel = (DesktopSource<MouseListener, AxisControl>) mouse.wheel();
		}

		@Test
		void constructor() {
			assertNotNull(wheel);
		}

		@Test
		void listener() {
			final Consumer<AxisControl> handler = mock(Consumer.class);
			final MouseListener listener = wheel.listener(handler);
			assertNotNull(listener);
			listener.event(null, 1, 2);
			verify(handler).accept((AxisControl) wheel);
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
