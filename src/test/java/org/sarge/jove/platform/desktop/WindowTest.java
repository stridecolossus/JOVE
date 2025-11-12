package org.sarge.jove.platform.desktop;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.foreign.*;
import org.sarge.jove.platform.desktop.DesktopTest.MockDesktopLibrary;
import org.sarge.jove.platform.desktop.Window.Hint;

class WindowTest {
	private static class MockWindowLibrary implements WindowLibrary {
		private Map<Integer, Integer> hints;
		private Dimensions size = new Dimensions(100, 200);
		private boolean destroyed;
		private String title;

		@Override
		public void glfwDefaultWindowHints() {
			hints = new HashMap<>();
		}

		@Override
		public void glfwWindowHint(int hint, int value) {
			hints.put(hint, value);
		}

		@Override
		public Handle glfwCreateWindow(int w, int h, String title, Handle monitor, Window shared) {
			assertEquals(100, w);
			assertEquals(200, h);
			assertEquals("title", title);
			assertEquals(null, monitor);
			assertEquals(null, shared);
			return new Handle(1);
		}

		@Override
		public int glfwCreateWindowSurface(Handle instance, Window window, Handle allocator, Pointer surface) {
			assertEquals(new Handle(1), window.handle());
			assertEquals(new Handle(2), instance);
			assertEquals(null, allocator);
			surface.set(new Handle(3));
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
	}

	private Window window;
	private MockWindowLibrary library;

	@BeforeEach
	void before() {
		library = new MockWindowLibrary();
		window = new Window(new Handle(1), library);
	}

	@Test
	void constructor() {
		assertEquals(false, library.destroyed);
		assertEquals(false, window.isDestroyed());
	}

	@Test
	void size() {
		assertEquals(new Dimensions(100, 200), window.size());
	}

	@Test
	void resize() {
		final var size = new Dimensions(300, 400);
		window.size(size);
		assertEquals(size, window.size());
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
				public Handle glfwCreateWindow(int w, int h, String title, Handle monitor, Window shared) {
					return null;
				}
			};
			assertThrows(RuntimeException.class, () -> builder.build(desktop));
		}
	}
}
