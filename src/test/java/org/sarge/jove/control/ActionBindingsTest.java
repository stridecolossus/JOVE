package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.control.Axis.AxisEvent;
import org.sarge.jove.control.DefaultButton.Action;
import org.sarge.jove.control.Event.Source;

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

	@Test
	void add() {
		bindings.add(handler);
		assertArrayEquals(new Object[]{handler}, bindings.handlers().toArray());
		assertNotNull(bindings.bindings(handler));
		assertEquals(0, bindings.bindings(handler).count());
		assertEquals(Optional.empty(), bindings.binding(src));
	}

	@Test
	void addDuplicate() {
		bindings.add(handler);
		assertThrows(IllegalArgumentException.class, () -> bindings.add(handler));
	}

	@Test
	void bind() {
		bindings.bind(src, handler);
		assertEquals(Optional.of(handler), bindings.binding(src));
		assertArrayEquals(new Object[]{src}, bindings.bindings(handler).toArray());
		assertArrayEquals(new Object[]{handler}, bindings.handlers().toArray());
	}

	@Test
	void bindNotAdded() {
		assertThrows(IllegalArgumentException.class, () -> bindings.bindings(handler));
	}

	@Test
	void bindDuplicateEventType() {
		bindings.bind(src, handler);
		assertThrows(IllegalArgumentException.class, () -> bindings.bind(src, handler));
	}

	@Test
	void bindSelf() {
		assertThrows(IllegalArgumentException.class, () -> bindings.bind(src, bindings));
	}

	@Nested
	class BindingHelpers {
		@Test
		void button() {
			// Bind a button template to a method
			final Runnable method = mock(Runnable.class);
			final Button button = new DefaultButton("button", Action.PRESS);
			bindings.bind(button, method);

			// Check matching button is delegated to the handler
			bindings.accept(button);
			verify(method).run();

			// Check unmatched event is ignored
			bindings.accept(button.resolve(0));
			verifyNoMoreInteractions(method);
		}

		@Test
		void position() {
			final Source<PositionEvent> src = mock(Source.class);
			final PositionEvent.Handler handler = mock(PositionEvent.Handler.class);
			bindings.bind(src, handler);
			bindings.accept(new PositionEvent(src, 1, 2));
			verify(handler).handle(1, 2);
			verify(src).bind(bindings);
		}

		@Test
		void axis() {
			final Axis axis = mock(Axis.class);
			final Axis.Handler handler = mock(Axis.Handler.class);
			bindings.bind(axis, handler);
			bindings.accept(new AxisEvent(axis, 3));
			verify(handler).handle(3);
			verify(axis).bind(bindings);
		}
	}

	@Test
	void remove() {
		bindings.bind(src, handler);
		bindings.remove(src);
		assertEquals(0, bindings.bindings(handler).count());
		assertEquals(Optional.empty(), bindings.binding(src));
	}

	@Test
	void removeNotBound() {
		assertThrows(IllegalArgumentException.class, () -> bindings.remove(src));
	}

	@Test
	void clear() {
		bindings.bind(src, handler);
		bindings.clear(handler);
		assertEquals(0, bindings.bindings(handler).count());
		assertEquals(Optional.empty(), bindings.binding(src));
	}

	@Test
	void clearNotBound() {
		assertThrows(IllegalArgumentException.class, () -> bindings.clear(handler));
	}

	@Test
	void clearAll() {
		bindings.bind(src, handler);
		bindings.clear();
		assertEquals(0, bindings.bindings(handler).count());
		assertEquals(Optional.empty(), bindings.binding(src));
	}

	@Test
	void accept() {
		final Event event = mock(Event.class);
		when(event.type()).thenReturn(src);
		bindings.bind(src, handler);
		bindings.accept(event);
		verify(handler).accept(event);
	}

	@Test
	void acceptUnknownEvent() {
		bindings.bind(src, handler);
		bindings.accept(mock(Event.class));
		verifyNoMoreInteractions(handler);
	}
}
