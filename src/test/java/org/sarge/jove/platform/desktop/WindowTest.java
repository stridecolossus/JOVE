package org.sarge.jove.platform.desktop;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.Set;
import java.util.function.IntBinaryOperator;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.*;
import org.sarge.jove.control.WindowListener;
import org.sarge.jove.platform.desktop.DesktopLibraryWindow.*;
import org.sarge.jove.platform.util.ReferenceFactory;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public class WindowTest {
	private Window window;
	private DesktopLibrary lib;
	private Desktop desktop;

	@BeforeEach
	void before() {
		// Init native library
		lib = mock(DesktopLibrary.class);

		// Init desktop
		desktop = mock(Desktop.class);
		when(desktop.library()).thenReturn(lib);

		// Init window descriptor
		final Window.Descriptor descriptor = new Window.Descriptor("title", new Dimensions(640, 480), Set.of(Window.Hint.DECORATED));

		// Create window
		window = new Window(new Handle(1), desktop, descriptor);
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
		assertEquals(Set.of(Window.Hint.DECORATED), window.descriptor().hints());

		// Check devices
		assertNotNull(window.keyboard());
		assertNotNull(window.mouse());
	}

	@Test
	void failed() {
		final var builder = new Window.Builder()
				.title("title")
				.size(new Dimensions(1, 2));

		assertThrows(RuntimeException.class, () -> builder.build(desktop));
	}

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

	@Test
	void surface() {
		// Init reference factory
		final Pointer ptr = new Pointer(1);
		final var ref = new PointerByReference(ptr);
		final var factory = mock(ReferenceFactory.class);
		when(desktop.factory()).thenReturn(factory);
		when(factory.pointer()).thenReturn(ref);

		// Create surface for this window
		final Handle instance = new Handle(new Pointer(2));
		final Handle surface = window.surface(instance);
		assertEquals(new Handle(ptr), surface);
		verify(lib).glfwCreateWindowSurface(instance, window, null, ref);
	}

	@Test
	void surfaceFailed() {
		// Init reference factory
		final var ref = new PointerByReference();
		final var factory = mock(ReferenceFactory.class);
		when(desktop.factory()).thenReturn(factory);
		when(factory.pointer()).thenReturn(ref);

		// Checks for unavailable surface
		final Handle instance = new Handle(new Pointer(2));
		when(lib.glfwCreateWindowSurface(instance, window, null, ref)).thenReturn(999);
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

			// Construct window
			window = builder
					.title("title")
					.size(new Dimensions(640, 480))
					.hint(Window.Hint.DECORATED)
					.build(desktop);

			// Check window
			assertNotNull(window);
			assertEquals(new Handle(ptr), window.handle());
			assertEquals(false, window.isDestroyed());
			verify(lib).glfwWindowHint(0x00020005, 1);
		}
	}
}
