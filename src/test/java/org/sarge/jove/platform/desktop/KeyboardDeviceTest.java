package org.sarge.jove.platform.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.desktop.Button.*;
import org.sarge.jove.platform.desktop.DeviceLibrary.KeyListener;

class KeyboardDeviceTest {
	private KeyboardDevice keyboard;
	private MockWindow window;
	private AtomicReference<ButtonEvent> key;
	private Consumer<ButtonEvent> listener;

	@BeforeEach
	void before() {
		key = new AtomicReference<>();
		listener = key::set;
		window = new MockWindow(new MockDeviceLibrary());
		keyboard = new KeyboardDevice(window);
	}

	@Test
	void bind() {
		keyboard.bind(listener);
		final var callback = (KeyListener) window.listeners().get(listener);
		callback.key(null, 256, 0, 1, 0);
		assertEquals(new ButtonEvent(new Button(256, "ESCAPE"), Action.PRESS, Set.of()), key.get());
	}

	@Test
	void remove() {
		keyboard.bind(listener);
		keyboard.remove(listener);
		assertEquals(false, window.listeners().containsKey(listener));
	}
}
