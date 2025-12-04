package org.sarge.jove.platform.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.junit.jupiter.api.*;
import org.sarge.jove.control.ScreenCoordinate;
import org.sarge.jove.platform.desktop.DeviceLibrary.MouseListener;

class MousePointerTest {
	private MousePointer pointer;
	private MockWindow window;
	private AtomicReference<ScreenCoordinate> coordinate;
	private Consumer<ScreenCoordinate> listener;

	@BeforeEach
	void before() {
		coordinate = new AtomicReference<>();
		listener = coordinate::set;
		window = new MockWindow(new MockDeviceLibrary());
		pointer = new MousePointer(window);
	}

	@Test
	void bind() {
		pointer.bind(listener);
		final var callback = (MouseListener) window.listeners().get(listener);
		callback.event(null, 2, 3);
		assertEquals(new ScreenCoordinate(2, 3), coordinate.get());
	}

	@Test
	void remove() {
		pointer.bind(listener);
		pointer.remove(listener);
		assertEquals(false, window.listeners().containsKey(listener));
	}
}
