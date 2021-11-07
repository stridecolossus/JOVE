package org.sarge.jove.platform.desktop;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;
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

public class KeyboardDeviceTest {
	private KeyboardDevice dev;
	private Window window;
	private DesktopLibrary lib;
	private DesktopSource<KeyListener> keyboard;

	@BeforeEach
	void before() {
		lib = mock(DesktopLibrary.class);
		window = mock(Window.class);
		dev = new KeyboardDevice(window);
		keyboard = (DesktopSource<KeyListener>) dev.keyboard();
	}

	@Test
	void keyboard() {
		assertNotNull(keyboard);
	}

	@Test
	void sources() {
		assertNotNull(dev.sources());
		assertArrayEquals(new Object[]{keyboard}, dev.sources().toArray());
	}

	@Test
	void key() {
		assertEquals(new Button("ESCAPE", keyboard), dev.key("ESCAPE"));
	}

	@Test
	void keyUnknown() {
		assertThrows(IllegalArgumentException.class, () -> dev.key("COBBLERS"));
	}

	@Nested
	class KeyboardSourceTests {
		@Test
		void constructor() {
			assertEquals(List.of(), keyboard.types());
		}

		@Test
		void listener() {
			final Consumer<Event> handler = mock(Consumer.class);
			final KeyListener listener = keyboard.listener(handler);
			listener.key(null, 256, 0, 1, 0x0001);
			verify(handler).accept(new Button("ESCAPE", keyboard, Action.PRESS, 0x0001));
		}

		@Test
		void method() {
			final KeyListener listener = mock(KeyListener.class);
			final BiConsumer<Window, KeyListener> method = keyboard.method(lib);
			assertNotNull(method);
			method.accept(window, listener);
			verify(lib).glfwSetKeyCallback(window, listener);
		}
	}
}
