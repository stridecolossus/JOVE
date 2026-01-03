package org.sarge.jove.platform.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.junit.jupiter.api.*;
import org.sarge.jove.control.AxisEvent;

class MouseWheelTest {
	private MouseWheel wheel;
	private MockWindow window;
	private AtomicReference<Float> integer;
	private Consumer<AxisEvent> listener;

	@BeforeEach
	void before() {
		integer = new AtomicReference<>();
		listener = event -> integer.set(event.value());
		window = new MockWindow(new MockDeviceLibrary());
		wheel = new MouseWheel(window);
	}

	@Test
	void bind() {
		final var callback = wheel.bind(listener);
		callback.event(null, 0, 3);
		assertEquals(3, integer.get());
	}

	@Test
	void remove() {
		wheel.bind(listener);
		wheel.remove();
		assertEquals(false, wheel.isBound());
	}
}
