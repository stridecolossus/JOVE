package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.junit.jupiter.api.*;
import org.sarge.jove.foreign.Callback;

class ActionBindingsTest {
	private static class MockDevice implements Device<AxisEvent> {
		private Consumer<AxisEvent> listener;

		@Override
		public Callback bind(Consumer<AxisEvent> listener) {
			assertEquals(null, this.listener);
			this.listener = listener;
			return null;
		}

		@Override
		public void remove() {
			assertNotNull(this.listener);
			this.listener = null;
		}
	}

	private ActionBindings bindings;
	private Action<AxisEvent> action;
	private MockDevice device;
	private AtomicReference<AxisEvent> listener;

	@BeforeEach
	void before() {
		listener = new AtomicReference<>();
		action = new Action<>("action", AxisEvent.class, listener::set);
		device = new MockDevice();
		bindings = new ActionBindings(List.of(action));
	}

	@Test
	void actions() {
		assertEquals(Set.of(action), bindings.actions());
	}

	@Nested
	class Empty {
		@Test
		void bindings() {
			assertEquals(List.of(), bindings.bindings(action));
			assertEquals(Optional.empty(), bindings.action(device));
		}

		@Test
		void bind() {
			bindings.bind(action, device);
		}

		@Test
		void uknown() {
			final var unknown = new Action<>("other", AxisEvent.class, listener::set);
			assertThrows(IllegalArgumentException.class, () -> bindings.bind(unknown, device));
		}

		@Test
		void remove() {
			assertThrows(IllegalArgumentException.class, () -> bindings.remove(action, device));
		}

		@Test
		void clear() {
			bindings.clear(action);
			bindings.clear();
		}
	}

	@Nested
	class Bound {
		@BeforeEach
		void before() {
			bindings.bind(action, device);
		}

		@Test
		void bindings() {
			assertEquals(List.of(device), bindings.bindings(action));
			assertEquals(Optional.of(action), bindings.action(device));
			assertNotNull(device.listener);
		}

		@Test
		void bind() {
			assertThrows(IllegalStateException.class, () -> bindings.bind(action, device));
		}

		@Test
		void remove() {
			bindings.remove(action, device);
			assertEquals(List.of(), bindings.bindings(action));
			assertEquals(Optional.empty(), bindings.action(device));
			assertEquals(null, device.listener);
		}

		@Test
		void clear() {
			bindings.clear(action);
			assertEquals(List.of(), bindings.bindings(action));
			assertEquals(Optional.empty(), bindings.action(device));
			assertEquals(null, device.listener);
		}

		@Test
		void all() {
			bindings.clear();
			assertEquals(List.of(), bindings.bindings(action));
			assertEquals(Optional.empty(), bindings.action(device));
			assertEquals(null, device.listener);
		}

		@Test
		void event() {
			final var event = new AxisEvent(2);
			device.listener.accept(event);
			assertEquals(event, listener.get());
		}
	}
}
