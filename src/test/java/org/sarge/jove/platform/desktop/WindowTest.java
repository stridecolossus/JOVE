package org.sarge.jove.platform.desktop;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.MemorySegment;
import java.util.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.sarge.jove.common.*;
import org.sarge.jove.control.*;
import org.sarge.jove.foreign.*;
import org.sarge.jove.platform.desktop.DesktopTest.MockDesktopLibrary;
import org.sarge.jove.platform.desktop.Window.Hint;
import org.sarge.jove.platform.desktop.WindowLibrary.WindowStateListener;

class WindowTest {
	static class MockWindowLibrary implements WindowLibrary {
		private Map<Integer, Integer> hints;
		private Dimensions size = new Dimensions(100, 200);
		private boolean destroyed;
		private String title;
		private WindowStateListener listener;

		@Override
		public void glfwDefaultWindowHints() {
			hints = new HashMap<>();
		}

		@Override
		public void glfwWindowHint(int hint, int value) {
			hints.put(hint, value);
		}

		@Override
		public Handle glfwCreateWindow(int w, int h, String title, Monitor monitor, Window shared) {
			assertEquals(100, w);
			assertEquals(200, h);
			assertEquals("title", title);
			assertEquals(null, monitor);
			assertEquals(null, shared);
			return new Handle(1);
		}

		@Override
		public int glfwCreateWindowSurface(Handle instance, Window window, Handle allocator, Pointer surface) {
			surface.set(MemorySegment.ofAddress(3));
			return 0;
		}

		@Override
		public boolean glfwWindowShouldClose(Window window) {
			return false;
		}

		@Override
		public void glfwSetWindowTitle(Window window, String title) {
			assertEquals(new Handle(1), window.handle());
			this.title = title;
		}

		@Override
		public void glfwGetWindowSize(Window window, IntegerReference w, IntegerReference h) {
			assertEquals(new Handle(1), window.handle());
			w.set(size.width());
			h.set(size.height());
		}

		@Override
		public void glfwGetFramebufferSize(Window window, IntegerReference w, IntegerReference h) {
			assertEquals(new Handle(1), window.handle());
			w.set(size.width());
			h.set(size.height());
		}

		@Override
		public void glfwSetWindowSize(Window window, int w, int h) {
			assertEquals(new Handle(1), window.handle());
			size = new Dimensions(w, h);
		}

		@Override
		public void glfwDestroyWindow(Window window) {
			assertEquals(new Handle(1), window.handle());
			assertEquals(false, destroyed);
			destroyed = true;
		}

		@Override
		public void glfwSetWindowShouldClose(Window window, boolean close) {
		}

		@Override
		public void glfwSetWindowCloseCallback(Window window, WindowStateListener listener) {
			this.listener = listener;
		}

		@Override
		public void glfwSetWindowFocusCallback(Window window, WindowStateListener listener) {
			this.listener = listener;
		}

		@Override
		public void glfwSetCursorEnterCallback(Window window, WindowStateListener listener) {
			this.listener = listener;
		}

		@Override
		public void glfwSetWindowIconifyCallback(Window window, WindowStateListener listener) {
			this.listener = listener;
		}

		@Override
		public void glfwSetWindowSizeCallback(Window window, WindowResizeListener listener) {
		}
	}

	private Window window;
	private MockWindowLibrary library;

	@BeforeEach
	void before() {
		KeyTable.Instance.INSTANCE.table(new KeyTable(Map.of(42, "key")));
		library = new MockWindowLibrary();
		window = new Window(new Handle(1), library);
	}

	@Test
	void constructor() {
		assertEquals(false, library.destroyed);
		assertEquals(false, window.isDestroyed());
	}

	@Test
	void keyboard() {
		assertEquals(Map.of(42, new Button(42, "key")), window.keyboard().keys());
	}

	@Test
	void mouse() {
		assertNotNull(window.mouse().buttons());
		assertNotNull(window.mouse().pointer());
		assertNotNull(window.mouse().wheel());
	}

	@ParameterizedTest
	@EnumSource
	void size(Window.Unit unit) {
		assertEquals(new Dimensions(100, 200), window.size(unit));
	}

	@Test
	void resize() {
		final var size = new Dimensions(300, 400);
		window.size(size);
		assertEquals(size, window.size(Window.Unit.SCREEN_COORDINATE));
	}

	@Test
	void title() {
		window.title("title");
		assertEquals("title", library.title);
	}

	@Nested
	class SurfaceTest {
    	@Test
    	void surface() {
    		assertEquals(new Handle(3), window.surface(new Handle(2)));
    	}

    	@Test
    	void failed() {
			library = new MockWindowLibrary() {
				@Override
				public int glfwCreateWindowSurface(Handle instance, Window window, Handle allocator, Pointer surface) {
					return 999;
				}
			};
			window = new Window(new Handle(1), library);
    		assertThrows(RuntimeException.class, () -> window.surface(new Handle(2)));
    	}
	}

	@Nested
	class Listeners {
		private static class MockWindowStateListener implements WindowStateListener {
			@Override
			public void state(MemorySegment window, boolean state) {
				// Empty
			}
		}

		private MockWindowStateListener listener;

		@BeforeEach
		void before() {
			listener = new MockWindowStateListener();
		}

		@ParameterizedTest
		@EnumSource
		void add(WindowStateListener.Type type) {
			window.listener(type, listener);
			assertEquals(listener, library.listener);
		}

		@Test
		void remove() {
			window.listener(WindowStateListener.Type.ENTER, listener);
			window.listener(WindowStateListener.Type.ENTER, null);
			assertEquals(null, library.listener);
		}
	}

	@Test
	void destroy() {
		window.destroy();
		assertEquals(true, library.destroyed);
		assertEquals(true, window.isDestroyed());
	}

	@Nested
	class BuilderTest {
		private Window.Builder builder;
		private Desktop desktop;

		@BeforeEach
		void before() {
			builder = new Window.Builder()
        			.hint(Hint.VISIBLE, 1)
        			.hint(Hint.CLIENT_API, 0)
        			.size(new Dimensions(100, 200))
        			.title("title");

			desktop = new Desktop(new MockDesktopLibrary()) {
				@Override
				public <T> T library() {
					return (T) library;
				}
			};
			// TODO - argh
		}

		@Test
		void build() {
			final Window window = builder.build(desktop);
			assertEquals(false, window.isDestroyed());
			assertEquals(Map.of(0x00020004, 1, 0x00022001, 0), library.hints);
		}

		@Test
		void failed() {
			library = new MockWindowLibrary() {
				@Override
				public Handle glfwCreateWindow(int w, int h, String title, Monitor monitor, Window shared) {
					return null;
				}
			};
			assertThrows(RuntimeException.class, () -> builder.build(desktop));
		}
	}
}
