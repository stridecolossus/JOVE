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
		assertEquals(false, device.isBound());
	}

	@Test
	void bind() {
		final var callback = device.bind(listener::set);
		assertEquals(true, device.isBound());
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
		device.bind(listener::set);
		device.remove();
		assertEquals(false, device.isBound());
	}

	@Test
	void unbound() {
		assertThrows(IllegalStateException.class, () -> device.remove());
	}
}
