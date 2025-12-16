package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.junit.jupiter.api.*;
import org.sarge.jove.control.Button.*;
import org.sarge.jove.foreign.Callback;

class ButtonDeviceAdapterTest {
	private interface MockCallback extends Callback {
		public void event();
	}

	private static class MockButtonDevice implements Device<ButtonEvent> {
		private Callback callback;

		@Override
		public boolean isBound() {
			return Objects.nonNull(callback);
		}

		@Override
		public Callback bind(Consumer<ButtonEvent> listener) {
			assertEquals(null, callback);

			callback = new MockCallback() {
				@Override
				public void event() {
					listener.accept(new ButtonEvent(new Button(1, "button"), ButtonAction.PRESS, Set.of()));
				}
			};

			return callback;
		}

		@Override
		public void remove() {
			callback = null;
		}
	}

	private ButtonDeviceAdapter adapter;
	private MockButtonDevice parent;
	private Device<ButtonEvent> device;
	private AtomicReference<ButtonEvent> listener;

	@BeforeEach
	void before() {
		listener = new AtomicReference<>();
		parent = new MockButtonDevice();
		adapter = new ButtonDeviceAdapter(parent);
		device = adapter.button(1);
	}

	@Test
	void bind() {
		final var callback = (MockCallback) device.bind(listener::set);
		callback.event();
		assertEquals(1, listener.get().button().index());
		assertEquals(callback, parent.callback);
	}

	@Test
	void multiple() {
		final Callback callback = device.bind(listener::set);
		assertSame(callback, adapter.button(2).bind(listener::set));
	}

	@Test
	void remove() {
		device.bind(listener::set);
		device.remove();
		assertEquals(null, parent.callback);
	}
}
