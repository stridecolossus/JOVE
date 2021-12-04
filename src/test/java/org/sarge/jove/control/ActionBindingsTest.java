package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Optional;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.control.Axis.AxisEvent;
import org.sarge.jove.control.Event.Source;

public class ActionBindingsTest {
	private ActionBindings bindings;
	private Axis axis;
	private Source src;
	private Consumer<AxisEvent> handler;

	@BeforeEach
	void before() {
		bindings = new ActionBindings();
		src = mock(Source.class);
		axis = mock(Axis.class);
		handler = mock(Consumer.class);
	}

	@Test
	void constructor() {
		assertNotNull(bindings.handlers());
		assertEquals(0, bindings.handlers().count());
		assertEquals(Optional.empty(), bindings.binding(axis));
	}

	@Test
	void add() {
		bindings.add(handler);
		assertArrayEquals(new Object[]{handler}, bindings.handlers().toArray());
		assertNotNull(bindings.bindings(handler));
		assertEquals(0, bindings.bindings(handler).count());
		assertEquals(Optional.empty(), bindings.binding(axis));
	}

	@Test
	void addDuplicate() {
		bindings.add(handler);
		assertThrows(IllegalArgumentException.class, () -> bindings.add(handler));
	}

	@Test
	void bind() {
		bindings.bind(axis, handler);
		assertEquals(Optional.of(handler), bindings.binding(axis));
		assertArrayEquals(new Object[]{axis}, bindings.bindings(handler).toArray());
		assertArrayEquals(new Object[]{handler}, bindings.handlers().toArray());
	}

	@Test
	void bindNotAdded() {
		assertThrows(IllegalArgumentException.class, () -> bindings.bindings(handler));
	}

	@Test
	void bindDuplicateHandler() {
		bindings.bind(axis, handler);
		assertThrows(IllegalArgumentException.class, () -> bindings.bind(axis, handler));
	}

	@Test
	void bindSelf() {
		assertThrows(IllegalArgumentException.class, () -> bindings.bind(new Object(), bindings));
	}

	@Nested
	class BindingHelpers {
		@Test
		void button() {
			final Button button = new Button("button");
			final Runnable method = mock(Runnable.class);
			final var adapter = bindings.bind(button, method);
			assertNotNull(adapter);
			bindings.accept(button);
			verify(method).run();
		}

		@Test
		void position() {
			final PositionEvent.Handler handler = mock(PositionEvent.Handler.class);
			final var adapter = bindings.bind(src, handler);
			assertNotNull(adapter);
			bindings.accept(new PositionEvent(src, 1, 2));
			verify(handler).handle(1, 2);
			verify(src).bind(bindings);
		}

		@Test
		void axis() {
			final Axis.Handler handler = mock(Axis.Handler.class);
			final var adapter = bindings.bind(axis, handler);
			assertNotNull(adapter);
			bindings.accept(new AxisEvent(axis, 3));
			verify(handler).handle(3);
			verify(axis).bind(bindings);
		}
	}

	@Test
	void remove() {
		bindings.bind(axis, handler);
		bindings.remove(axis);
		assertEquals(0, bindings.bindings(handler).count());
		assertEquals(Optional.empty(), bindings.binding(axis));
	}

	@Test
	void removeNotBound() {
		assertThrows(IllegalArgumentException.class, () -> bindings.remove(axis));
	}

	@Test
	void clear() {
		bindings.bind(axis, handler);
		bindings.clear(handler);
		assertEquals(0, bindings.bindings(handler).count());
		assertEquals(Optional.empty(), bindings.binding(axis));
	}

	@Test
	void clearNotBound() {
		assertThrows(IllegalArgumentException.class, () -> bindings.clear(handler));
	}

	@Test
	void clearAll() {
		bindings.bind(axis, handler);
		bindings.clear();
		assertEquals(0, bindings.bindings(handler).count());
		assertEquals(Optional.empty(), bindings.binding(axis));
	}

	@Test
	void accept() {
		final AxisEvent event = new AxisEvent(axis, 3);
		bindings.bind(axis, handler);
		bindings.accept(event);
		verify(handler).accept(event);
	}

	@Test
	void acceptUnknownEvent() {
		bindings.bind(axis, handler);
		bindings.accept(mock(Event.class));
		verifyNoMoreInteractions(handler);
	}
}
