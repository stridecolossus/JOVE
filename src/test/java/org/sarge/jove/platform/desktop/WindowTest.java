package org.sarge.jove.platform.desktop;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.function.IntBinaryOperator;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.*;
import org.sarge.jove.control.WindowListener;
import org.sarge.jove.platform.desktop.DesktopLibraryWindow.*;
import org.sarge.jove.util.*;

import com.sun.jna.Pointer;

class WindowTest {
	private Window window;
	private DesktopLibrary lib;
	private Desktop desktop;
	private ReferenceFactory factory;

	@BeforeEach
	void before() {
		lib = mock(DesktopLibrary.class);
		factory = new MockReferenceFactory();
		desktop = new Desktop(lib, factory);
		window = new Window(new Handle(1), desktop);
	}

	@Test
	void constructor() {
		assertEquals(new Handle(1), window.handle());
		assertEquals(desktop, window.desktop());
	}

	@Test
	void devices() {
		assertNotNull(window.keyboard());
		assertNotNull(window.mouse());
	}

	@DisplayName("A window cannot be created if the native library returns a NULL window pointer")
	@Test
	void failed() {
		final var builder = new Window.Builder();
		builder.title("title");
		builder.size(new Dimensions(1, 2));
		assertThrows(RuntimeException.class, () -> builder.build(desktop));
	}

	@DisplayName("A window can be resized")
	@Test
	void resize() {
		window.size(new Dimensions(2, 3));
		verify(lib).glfwSetWindowSize(window, 2, 3);
	}

	@DisplayName("The window title can be reset")
	@Test
	void title() {
		window.title("title");
		verify(lib).glfwSetWindowTitle(window, "title");
	}

	@DisplayName("A non-fullscreen window does not have a monitor")
	@Test
	void monitor() {
		assertEquals(Optional.empty(), window.monitor());
	}

	@Nested
	class FullScreenTests {
		private Monitor monitor;

		@BeforeEach
		void before() {
			monitor = new Monitor(new Handle(4), "name", new Dimensions(2, 3), List.of());
		}

		@DisplayName("A fullscreen window has a monitor")
		@Test
		void monitor() {
			when(lib.glfwGetWindowMonitor(window)).thenReturn(monitor);
			assertEquals(Optional.of(monitor), window.monitor());
		}

		@DisplayName("A window can be made fullscreen")
		@Test
    	void full() {
			// TODO - implement
			// TOOD - separate tests into windowed and full-screen?
    	}
	}

	@Nested
	class ListenerTests {
    	@ParameterizedTest
    	@EnumSource(WindowListener.Type.class)
    	void listener(WindowListener.Type type) {
    		// Register state-change listener
    		final WindowListener listener = mock(WindowListener.class);
    		window.listener(type, listener);

    		// Check API
    		final var captor = ArgumentCaptor.forClass(WindowStateListener.class);
    		switch(type) {
    			case ENTER -> verify(lib).glfwSetCursorEnterCallback(eq(window), captor.capture());
    			case FOCUS -> verify(lib).glfwSetWindowFocusCallback(eq(window), captor.capture());
    			case ICONIFIED -> verify(lib).glfwSetWindowIconifyCallback(eq(window), captor.capture());
    			case CLOSED -> verify(lib).glfwSetWindowCloseCallback(eq(window), captor.capture());
    		}

    		// Check listener
    		final WindowStateListener adapter = captor.getValue();
    		adapter.state(null, 1);
    		verify(listener).state(type, true);
    	}

    	@Test
    	void resize() {
    		// Register resize listener
    		final var listener = mock(IntBinaryOperator.class);
    		window.resize(listener);

    		// Check API
    		final var captor = ArgumentCaptor.forClass(WindowResizeListener.class);
    		verify(lib).glfwSetWindowSizeCallback(eq(window), captor.capture());

    		// Check listener
    		final WindowResizeListener adapter = captor.getValue();
    		adapter.resize(null, 1, 2);
    		verify(listener).applyAsInt(1, 2);
    	}

    	@Test
    	void remove() {
    		final var type = WindowListener.Type.ENTER;
    		window.listener(type, null);
    		verify(lib).glfwSetCursorEnterCallback(window, null);
    	}
	}

	@Test
	void surface() {
		final Handle instance = new Handle(3);
		assertNotNull(window.surface(instance));
		verify(lib).glfwCreateWindowSurface(instance, window, null, factory.pointer());
	}

	@Test
	void surfaceFailed() {
		final Handle instance = new Handle(3);
		when(lib.glfwCreateWindowSurface(instance, window, null, factory.pointer())).thenReturn(999);
		assertThrows(RuntimeException.class, () -> window.surface(instance));
	}

	@Test
	void destroy() {
		window.destroy();
		verify(lib).glfwDestroyWindow(window);
	}

	@Nested
	class BuilderTests {
		private Window.Builder builder;

		@BeforeEach
		void before() {
			builder = new Window.Builder();
		}

		@Test
		void build() {
			// Init API
			final Pointer ptr = new Pointer(1);
			when(lib.glfwCreateWindow(640, 480, "title", null, null)).thenReturn(ptr);

			// Construct a window without decorations
			window = builder
					.title("title")
					.size(new Dimensions(640, 480))
					.hint(Window.Hint.DECORATED)
					.build(desktop);

			// Check window
			assertEquals(new Handle(ptr), window.handle());
			assertEquals(false, window.isDestroyed());
			verify(lib).glfwWindowHint(0x00020005, 0);
		}
	}
}
