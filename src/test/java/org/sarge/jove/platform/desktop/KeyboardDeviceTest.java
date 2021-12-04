package org.sarge.jove.platform.desktop;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.control.Button;
import org.sarge.jove.control.Button.Action;
import org.sarge.jove.control.Event;
import org.sarge.jove.platform.desktop.DesktopDevice.DesktopSource;
import org.sarge.jove.platform.desktop.DesktopLibraryDevice.KeyListener;
import org.sarge.jove.util.IntegerEnumeration;

public class KeyboardDeviceTest {
	private KeyboardDevice dev;
	private Window window;
	private DesktopLibrary lib;

	@BeforeEach
	void before() {
		lib = mock(DesktopLibrary.class);
		window = mock(Window.class);
		dev = new KeyboardDevice(window);
	}

	@Test
	void keyboard() {
		assertNotNull(dev.keyboard());
	}

	@Test
	void sources() {
		assertNotNull(dev.sources());
		assertEquals(Set.of(dev.keyboard()), dev.sources());
	}

	@Test
	void key() {
		assertEquals(new Button("ESCAPE"), dev.key("ESCAPE"));
	}

	@Test
	void keyUnknown() {
		assertThrows(IllegalArgumentException.class, () -> dev.key("COBBLERS"));
	}

	@Test
	void bind() {
		final Consumer<Event> handler = mock(Consumer.class);
		final Desktop desktop = mock(Desktop.class);
		when(desktop.library()).thenReturn(lib);
		when(window.desktop()).thenReturn(desktop);
		dev.bind(handler);
	}

	@Nested
	class KeyboardSourceTests {
		private DesktopSource<KeyListener> src;

		@BeforeEach
		void before() {
			src = (DesktopSource<KeyListener>) dev.keyboard();
		}

		@Test
		void listener() {
			final int mods = IntegerEnumeration.mask(Button.Modifier.CONTROL);
			final Consumer<Event> handler = mock(Consumer.class);
			final KeyListener listener = src.listener(handler);
			listener.key(null, 256, 0, 1, mods);
			verify(handler).accept(new Button("ESCAPE", Action.PRESS, mods));
		}

		@Test
		void method() {
			final KeyListener listener = mock(KeyListener.class);
			final BiConsumer<Window, KeyListener> method = src.method(lib);
			assertNotNull(method);
			method.accept(window, listener);
			verify(lib).glfwSetKeyCallback(window, listener);
		}
	}
}
