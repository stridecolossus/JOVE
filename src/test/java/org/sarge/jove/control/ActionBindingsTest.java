package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.control.Axis.AxisEvent;
import org.sarge.jove.control.Button.ToggleHandler;
import org.sarge.jove.control.DefaultButton.Action;
import org.sarge.jove.control.Event.Source;
import org.sarge.jove.control.ModifiedButton.Modifier;

public class ActionBindingsTest {
	private ActionBindings bindings;
	private Source<Event> src;
	private Consumer<Event> handler;

	@BeforeEach
	void before() {
		bindings = new ActionBindings();
		handler = mock(Consumer.class);
		src = mock(Source.class);
	}

	@Test
	void constructor() {
		assertNotNull(bindings.handlers());
		assertEquals(0, bindings.handlers().count());
		assertEquals(Optional.empty(), bindings.binding(src));
	}

	@DisplayName("Register an action handler without any bindings")
	@Test
	void add() {
		bindings.add(handler);
		assertArrayEquals(new Object[]{handler}, bindings.handlers().toArray());
		assertNotNull(bindings.bindings(handler));
		assertEquals(0, bindings.bindings(handler).count());
		assertEquals(Optional.empty(), bindings.binding(src));
	}

	@DisplayName("Cannot register the same action handler more than once")
	@Test
	void addDuplicate() {
		bindings.add(handler);
		assertThrows(IllegalArgumentException.class, () -> bindings.add(handler));
	}

	@DisplayName("Binding an event source should also register the handler")
	@Test
	void bind() {
		bindings.bind(src, handler);
		assertEquals(Optional.of(handler), bindings.binding(src));
		assertArrayEquals(new Object[]{src}, bindings.bindings(handler).toArray());
		assertArrayEquals(new Object[]{handler}, bindings.handlers().toArray());
	}

	@DisplayName("Cannot add the same binding more than once")
	@Test
	void bindDuplicateEventType() {
		bindings.bind(src, handler);
		assertThrows(IllegalArgumentException.class, () -> bindings.bind(src, handler));
	}

	@DisplayName("Cannot bind the action bindings to itself")
	@Test
	void bindSelf() {
		assertThrows(IllegalArgumentException.class, () -> bindings.bind(src, bindings));
	}

	@DisplayName("Cannot retrieve a binding that has not been added")
	@Test
	void bindingNotAdded() {
		assertThrows(IllegalArgumentException.class, () -> bindings.bindings(handler));
	}

	@DisplayName("Bind a handler method to a position source")
	@Test
	void position() {
		final Source<PositionEvent> src = mock(Source.class);
		final PositionEvent.Handler handler = mock(PositionEvent.Handler.class);
		bindings.bind(src, handler);
		bindings.accept(new PositionEvent(src, 1, 2));
		verify(handler).handle(1, 2);
		verify(src).bind(bindings);
	}

	@DisplayName("Bind a handler method to an axis")
	@Test
	void axis() {
		final Axis axis = mock(Axis.class);
		final Axis.Handler handler = mock(Axis.Handler.class);
		bindings.bind(axis, handler);
		bindings.accept(new AxisEvent(axis, 3));
		verify(handler).handle(3);
		verify(axis).bind(bindings);
	}

	@Nested
	class ButtonTests {
		private Button button;

		@BeforeEach
		void before() {
			button = mock(Button.class);
			when(button.type()).thenReturn(button);
		}

		@DisplayName("Bind a handler method to a button that should match as a template")
		@Test
		void button() {
			// Bind a button template to a method
			final Runnable method = mock(Runnable.class);
			bindings.bind(button, method);

			// Check unmatched buttons are ignored
			bindings.accept(button);
			verifyNoInteractions(method);

			// Check matching button is delegated to the handler
			when(button.matches(button)).thenReturn(true);
			bindings.accept(button);
			verify(method).run();
		}

		@DisplayName("Bind a handler method to a button toggle")
		@Test
		void toggle() {
			// Bind a button template to a toggle method
			final ToggleHandler method = mock(ToggleHandler.class);
			bindings.bind(button, method);

			// Check unmatched buttons are ignored
			bindings.accept(button);
			verifyNoInteractions(method);

			// Check matching button press is delegated to the handler
			when(button.matches(button)).thenReturn(true);
			when(button.action()).thenReturn(Action.PRESS);
			bindings.accept(button);
			verify(method).handle(true);

			// Check button release
			when(button.action()).thenReturn(Action.RELEASE);
			bindings.accept(button);
			verify(method).handle(false);
		}

		@DisplayName("An unmatched modified button event should be delegated to the default button binding if present")
		@Test
		void unmodified() {
			// Bind the unmodified button
			final Runnable method = mock(Runnable.class);
			final Button def = new DefaultButton("button", Action.PRESS);
			bindings.bind(def, method);

			// Check the unmodified button is invoked
			final ModifiedButton mod = new ModifiedButton("button", Action.PRESS, Modifier.CONTROL.value());
			bindings.accept(mod);
			verify(method).run();
		}
	}

	@DisplayName("Can remove a binding")
	@Test
	void remove() {
		bindings.bind(src, handler);
		bindings.remove(src);
		assertEquals(0, bindings.bindings(handler).count());
		assertEquals(Optional.empty(), bindings.binding(src));
		assertArrayEquals(new Object[]{handler}, bindings.handlers().toArray());
	}

	@DisplayName("Cannot remove a binding that has not been added")
	@Test
	void removeNotBound() {
		assertThrows(IllegalArgumentException.class, () -> bindings.remove(src));
	}

	@DisplayName("Clear should remove the bindings but retain the handler")
	@Test
	void clear() {
		bindings.bind(src, handler);
		bindings.clear(handler);
		assertEquals(Optional.empty(), bindings.binding(src));
		assertEquals(0, bindings.bindings(handler).count());
		assertArrayEquals(new Object[]{handler}, bindings.handlers().toArray());
	}

	@DisplayName("Cannot clear the bindings for a handler that is not present")
	@Test
	void clearNotBound() {
		assertThrows(IllegalArgumentException.class, () -> bindings.clear(handler));
	}

	@DisplayName("Clear should remove all bindings but retain the action handlers")
	@Test
	void clearAll() {
		bindings.bind(src, handler);
		bindings.clear();
		assertEquals(Optional.empty(), bindings.binding(src));
		assertEquals(0, bindings.bindings(handler).count());
		assertArrayEquals(new Object[]{handler}, bindings.handlers().toArray());
	}

	@DisplayName("Event with a binding should be delegated to the bound action handler")
	@Test
	void accept() {
		final Event event = mock(Event.class);
		when(event.type()).thenReturn(src);
		bindings.bind(src, handler);
		bindings.accept(event);
		verify(handler).accept(event);
	}

	@DisplayName("Events that are not bound should be ignored")
	@Test
	void acceptUnknownEvent() {
		bindings.bind(src, handler);
		bindings.accept(mock(Event.class));
		verifyNoMoreInteractions(handler);
	}
}
