package org.sarge.jove.platform.desktop;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.MemorySegment;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.control.AxisEvent;
import org.sarge.jove.platform.desktop.DeviceLibrary.MouseListener;

class AbstractWindowDeviceTest {
	private static class MockAbstractWindowDevice extends AbstractWindowDevice <AxisEvent, MouseListener> {
		protected MockAbstractWindowDevice() {
			super(new MockWindow(new MockDeviceLibrary()));
		}

		@Override
		protected MouseListener callback(Window window, Consumer<AxisEvent> listener) {
			return new MouseListener() {
				@Override
				public void event(MemorySegment window, double x, double y) {
					listener.accept(new AxisEvent((int) y));
				}
			};
		}

		@Override
		protected BiConsumer<Window, MouseListener> method(DeviceLibrary library) {
			return library::glfwSetScrollCallback;
		}

	}

	private MockAbstractWindowDevice device;
	private AtomicReference<AxisEvent> listener;

	@BeforeEach
	void before() {
		listener = new AtomicReference<>();
		device = new MockAbstractWindowDevice();
	}

	@Test
	void none() {
		assertEquals(null, device.listener());
	}

	@Test
	void bind() {
		final var callback = device.bind(listener::set);
		assertNotNull(device.listener());
		callback.event(null, 0, 1);
		assertEquals(new AxisEvent(1), listener.get());
	}

	@Test
	void already() {
		device.bind(listener::set);
		assertThrows(IllegalStateException.class, () -> device.bind(listener::set));
	}

	@Test
	void remove() {
		final Consumer<AxisEvent> handler = listener::set;
		device.bind(handler);
		device.remove(handler);
		assertEquals(null, device.listener());
	}

	@Test
	void unbound() {
		assertThrows(IllegalArgumentException.class, () -> device.remove(listener::set));
	}
}
