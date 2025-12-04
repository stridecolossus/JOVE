package org.sarge.jove.platform.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.desktop.DeviceLibrary.MouseListener;

class MouseWheelTest {
	private MouseWheel wheel;
	private MockWindow window;
	private AtomicInteger integer;
	private Consumer<Integer> listener;

	@BeforeEach
	void before() {
		integer = new AtomicInteger();
		listener = integer::set;
		window = new MockWindow(new MockDeviceLibrary());
		wheel = new MouseWheel(window);
	}

	@Test
	void bind() {
		wheel.bind(listener);
		final var callback = (MouseListener) window.listeners().get(listener);
		callback.event(null, 0, 3);
		assertEquals(3, integer.get());
	}

	@Test
	void remove() {
		wheel.bind(listener);
		wheel.remove(listener);
		assertEquals(false, window.listeners().containsKey(listener));
	}
}
