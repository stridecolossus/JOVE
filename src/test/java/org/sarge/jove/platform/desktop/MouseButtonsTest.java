package org.sarge.jove.platform.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.junit.jupiter.api.*;
import org.sarge.jove.control.Button;
import org.sarge.jove.control.Button.*;
import org.sarge.jove.platform.desktop.DeviceLibrary.MouseButtonListener;

class MouseButtonsTest {
	private MouseButtons buttons;
	private MockWindow window;
	private AtomicReference<ButtonEvent> model;
	private Consumer<ButtonEvent> listener;

	@BeforeEach
	void before() {
		model = new AtomicReference<>();
		listener = model::set;
		window = new MockWindow(new MockDeviceLibrary());
		buttons = new MouseButtons(window);
	}

	@Test
	void bind() {
		// Bind a listener to the mouse buttons
		buttons.bind(listener);

		// Generate a button event
		final var callback = (MouseButtonListener) window.listeners().get(listener);
		callback.button(null, 2, 1, 0);

		// Check received event
		final Button button = new Button(2, "Mouse-2");
		final var event = new ButtonEvent(buttons, button, ButtonAction.PRESS, Set.of());
		assertEquals(event, model.get());
	}

	@Test
	void remove() {
		buttons.bind(listener);
		buttons.remove(listener);
		assertEquals(false, window.listeners().containsKey(listener));
	}
}
