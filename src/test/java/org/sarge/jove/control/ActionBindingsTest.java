package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.*;
import org.sarge.jove.control.Button.*;

class ActionBindingsTest {
	private ActionBindings bindings;
	private Action<ButtonEvent> action;
	private AtomicReference<ButtonEvent> handler;
	private Button button;
	private ButtonEvent event;

	@BeforeEach
	void before() {
		button = new Button(1, "button");
		event = new ButtonEvent(button, ButtonAction.PRESS, Set.of());
		handler = new AtomicReference<>();
		action = new Action<>("action", ButtonEvent.class, handler::set);
		bindings = new ActionBindings(Set.of(action));
	}

	@Test
	void name() {
		assertEquals(action, bindings.action("action"));
		assertThrows(NoSuchElementException.class, () -> bindings.action("cobblers"));
	}

	@Nested
	class None {
		@Test
		void bindings() {
			assertEquals(Map.of(action, List.of()), bindings.actions());
		}

		@Test
		void action() {
			assertEquals(Optional.empty(), bindings.action(button));
		}

		@Test
		void handle() {
			bindings.handle(event);
			assertEquals(null, handler.get());
		}

		@Test
		void bind() {
			bindings.bind(action, button);
		}

		@Test
		void type() {
			assertThrows(IllegalArgumentException.class, () -> bindings.bind(action, new Object()));
		}

		@Test
		void remove() {
			assertThrows(IllegalArgumentException.class, () -> bindings.remove(button));
		}

		@Test
		void clear() {
			bindings.clear(action);
			assertEquals(Map.of(action, List.of()), bindings.actions());
		}

		@Test
		void all() {
			bindings.clear();
			assertEquals(Map.of(action, List.of()), bindings.actions());
		}
	}

	@Nested
	class Bound {
		@BeforeEach
		void before() {
			bindings.bind(action, button);
		}

		@Test
		void bindings() {
			assertEquals(Map.of(action, List.of(button)), bindings.actions());
		}

		@Test
		void action() {
			assertEquals(Optional.of(action), bindings.action(button));
		}

		@Test
		void handle() {
			bindings.handle(event);
			assertEquals(event, handler.get());
		}

		@Test
		void ignored() {
			bindings.handle(new ButtonEvent(new Button(2, "other"), ButtonAction.PRESS, Set.of()));
			assertEquals(null, handler.get());
		}

		@Test
		void bind() {
			final var other = new Button(2, "other");
			bindings.bind(action, other);
			assertEquals(Map.of(action, List.of(button, other)), bindings.actions());
			assertEquals(Optional.of(action), bindings.action(button));
			assertEquals(Optional.of(action), bindings.action(other));
		}

		@Test
		void duplicate() {
			assertThrows(IllegalStateException.class, () -> bindings.bind(action, button));
		}

		@Test
		void remove() {
			bindings.remove(button);
			assertEquals(Map.of(action, List.of()), bindings.actions());
			assertEquals(Optional.empty(), bindings.action(button));
		}

		@Test
		void clear() {
			bindings.clear(action);
			assertEquals(Map.of(action, List.of()), bindings.actions());
			assertEquals(Optional.empty(), bindings.action(button));
		}

		@Test
		void all() {
			bindings.clear();
			assertEquals(Map.of(action, List.of()), bindings.actions());
			assertEquals(Optional.empty(), bindings.action(button));
		}
	}
}
