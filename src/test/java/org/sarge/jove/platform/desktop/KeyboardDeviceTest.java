package org.sarge.jove.platform.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.junit.jupiter.api.*;
import org.sarge.jove.control.Button;
import org.sarge.jove.control.Button.*;
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
		// Bind a listener to the keyboard
		keyboard.bind(listener);

		// Generate an event
		final var callback = (KeyListener) window.listeners().get(listener);
		callback.key(null, 256, 0, 1, 0x002);

		// Check event received by the listener
		final Button button = new Button(256, "ESCAPE");
		final var event = new ButtonEvent(keyboard, button, ButtonAction.PRESS, Set.of(ModifierKey.CONTROL));
		assertEquals(event, key.get());
	}

	@Test
	void remove() {
		keyboard.bind(listener);
		keyboard.remove(listener);
		assertEquals(false, window.listeners().containsKey(listener));
	}
}
