package org.sarge.jove.platform.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.desktop.Button.*;
import org.sarge.jove.platform.desktop.DeviceLibrary.MouseButtonListener;

class MouseButtonsTest {
	private MouseButtons buttons;
	private MockWindow window;
	private AtomicReference<ButtonEvent> event;
	private Consumer<ButtonEvent> listener;

	@BeforeEach
	void before() {
		event = new AtomicReference<>();
		listener = event::set;
		window = new MockWindow(new MockDeviceLibrary());
		buttons = new MouseButtons(window);
	}

	@Test
	void bind() {
		buttons.bind(listener);
		final var callback = (MouseButtonListener) window.listeners().get(listener);
		callback.button(null, 2, 1, 0b0010);
		assertEquals(new ButtonEvent(new Button(2, "Mouse-2"), Action.PRESS, Set.of(ModifierKey.CONTROL)), event.get());
	}

	@Test
	void remove() {
		buttons.bind(listener);
		buttons.remove(listener);
		assertEquals(false, window.listeners().containsKey(listener));
	}
}
