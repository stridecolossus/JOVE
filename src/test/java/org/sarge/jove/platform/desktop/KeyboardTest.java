package org.sarge.jove.platform.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.junit.jupiter.api.*;
import org.sarge.jove.control.*;
import org.sarge.jove.control.Button.*;

class KeyboardTest {
	private Keyboard keyboard;
	private MockWindow window;
	private AtomicReference<ButtonEvent> key;
	private Consumer<ButtonEvent> listener;

	@BeforeEach
	void before() {
		KeyTable.Instance.INSTANCE.table(new KeyTable(Map.of(42, "key")));
		key = new AtomicReference<>();
		listener = key::set;
		window = new MockWindow(new MockDeviceLibrary());
		keyboard = new Keyboard(window);
	}

	@Test
	void bind() {
		// Bind a listener to the keyboard
		final var callback = keyboard.bind(listener);

		// Generate an event
		callback.key(null, 42, 0, 1, 0x002);

		// Check event received by the listener
		final Button button = new Button(42, "key");
		final var event = new ButtonEvent( button, ButtonAction.PRESS, Set.of(ModifierKey.CONTROL));
		assertEquals(event, key.get());
	}

	@Test
	void remove() {
		keyboard.bind(listener);
		keyboard.remove();
		assertEquals(false, keyboard.isBound());
	}
}
