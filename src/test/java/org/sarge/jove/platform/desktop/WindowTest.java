package org.sarge.jove.platform.desktop;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Handle;
import org.sarge.jove.control.WindowListener;

import com.sun.jna.Pointer;

public class WindowTest {
	private Window window;
	private DesktopLibrary lib;
	private Desktop desktop;

	@BeforeEach
	void before() {
		// Init native library
		lib = mock(DesktopLibrary.class);
		when(lib.glfwCreateWindow(640, 480, "title", null, null)).thenReturn(new Pointer(1));

		// Init desktop
		desktop = mock(Desktop.class);
		when(desktop.library()).thenReturn(lib);

		// Init window descriptor
		final Window.Descriptor descriptor = new Window.Descriptor.Builder()
				.title("title")
				.size(new Dimensions(640, 480))
				.property(Window.Property.DECORATED)
				.build();

		// Create window
		window = Window.create(desktop, descriptor, null);
	}

	@Test
	void constructor() {
		// Check window
		assertNotNull(window.handle());
		assertEquals(desktop, window.desktop());

		// Check descriptor
		assertNotNull(window.descriptor());
		assertEquals("title", window.descriptor().title());
		assertEquals(new Dimensions(640, 480), window.descriptor().size());
		assertEquals(Set.of(Window.Property.DECORATED), window.descriptor().properties());

		// Check devices
		assertNotNull(window.keyboard());
		assertNotNull(window.mouse());

		// Check GLFW window hints applied
		verify(lib).glfwWindowHint(0x00020005, 1);
	}

	@Test
	void createFailed() {
		when(lib.glfwCreateWindow(640, 480, "title", null, null)).thenReturn(null);
		assertThrows(RuntimeException.class, () -> Window.create(desktop, window.descriptor(), null));
	}

	@Test
	void listener() {
		// Attach listener
		final WindowListener listener = mock(WindowListener.class);
		window.listener(listener);

		// TODO - how to check each underlying listener without lots of nasty code?

//		// Check API (for one of the underlying listeners)
//		final ArgumentCaptor<WindowStateListener> captor = ArgumentCaptor.forClass(WindowStateListener.class);
//		verify(lib).glfwSetCursorEnterCallback(eq(window), captor.capture());
//
//		// Check underlying listener
//		final WindowStateListener state = captor.getValue();
//		assertNotNull(state);
//
//		// Check listener delegation
//		state.state(window.handle().toPointer(), true);
//		verify(listener).cursor(true);
	}

	@Test
	void removeListener() {
		window.listener(null);
		verify(lib).glfwSetCursorEnterCallback(window, null);
		verify(lib).glfwSetWindowFocusCallback(window, null);
		verify(lib).glfwSetWindowIconifyCallback(window, null);
		verify(lib).glfwSetWindowResizeCallback(window, null);
	}

	@Test
	void surface() {
		final Handle instance = new Handle(new Pointer(2));
		final Handle surface = window.surface(instance);
		assertNotNull(surface);
	}

	@Test
	void destroy() {
		window.destroy();
		verify(lib).glfwDestroyWindow(window);
	}
}
