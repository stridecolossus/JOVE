package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.control.Action.Bindings;

class ActionTest {
	private Bindings bindings;
	private Event event;
	private Action<Event> action;

	@SuppressWarnings("unchecked")
	@BeforeEach
	void before() {
		bindings = new Bindings();
		action = mock(Action.class);
		event = mock(Event.class);
		when(event.type()).thenReturn(event);
	}

	@DisplayName("An empty set of bindings...")
	@Nested
	class EmptyBindings {
		@DisplayName("has no registered actions")
		@Test
		void actions() {
			assertEquals(Set.of(), bindings.actions());
		}

		@DisplayName("can register an action")
		@Test
		void add() {
			bindings.add(action);
			assertEquals(Set.of(action), bindings.actions());
		}

		@DisplayName("has no bound action for a given event")
		@Test
		void action() {
			assertEquals(Optional.empty(), bindings.action(event));
		}

		@DisplayName("does not have an bindings")
		@Test
		void bindings() {
			assertThrows(IllegalStateException.class, () -> bindings.bindings(action));
		}

		@DisplayName("can bind an event to an action")
		@Test
		void bind() {
			bindings.add(action);
			bindings.bind(event, action);
		}

		@DisplayName("cannot bind an event to an unregistered action")
		@Test
		void unknown() {
			assertThrows(IllegalStateException.class, () -> bindings.bind(event, action));
		}

		@DisplayName("cannot remove bindings")
		@Test
		void remove() {
			assertThrows(IllegalStateException.class, () -> bindings.remove(event));
			assertThrows(IllegalStateException.class, () -> bindings.remove(action));
		}

		@DisplayName("ignores all events")
		@Test
		void accept() {
			bindings.accept(event);
			verifyNoMoreInteractions(action);
		}
	}

	@DisplayName("An action binding...")
	@Nested
	class BoundAction {
		@BeforeEach
		void before() {
			bindings.add(action);
			bindings.bind(event, action);
		}

		@DisplayName("can be retrieved from the bindings")
		@Test
		void binding() {
			assertEquals(Optional.of(action), bindings.action(event));
			assertEquals(Set.of(event), bindings.bindings(action));
		}

		@DisplayName("routes matching events to the action")
		@Test
		void accept() {
			bindings.accept(event);
			verify(action).execute(event);
		}

		@DisplayName("can be bound to multiple events")
		@Test
		void multiple() {
			final Event other = mock(Event.class);
			bindings.bind(other, action);
			assertEquals(Set.of(event, other), bindings.bindings(action));
		}

		@DisplayName("cannot bind the event to another action")
		@Test
		void bound() {
			assertThrows(IllegalStateException.class, () -> bindings.bind(event, action));
		}

		@DisplayName("can remove a bound event")
		@Test
		void remove() {
			bindings.remove(event);
			assertEquals(Optional.empty(), bindings.action(event));
			assertEquals(Set.of(), bindings.bindings(action));
			assertEquals(Set.of(action), bindings.actions());
		}

		@DisplayName("can remove all bound events")
		@Test
		void clear() {
			bindings.remove(action);
			assertEquals(Optional.empty(), bindings.action(event));
			assertEquals(Set.of(), bindings.bindings(action));
			assertEquals(Set.of(action), bindings.actions());
		}
	}
}
