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
import org.sarge.jove.control.Event.Type;

public class ActionBindingsTest {
	private ActionBindings bindings;
	private Axis axis;
	private Source src;
	private Consumer<AxisEvent> handler;

	@BeforeEach
	void before() {
		bindings = new ActionBindings();
		src = mock(Source.class);
		axis = new Axis("axis", src);
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
		assertArrayEquals(new Type[]{axis}, bindings.bindings(handler).toArray());
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
		assertThrows(IllegalArgumentException.class, () -> bindings.bind(mock(Type.class), bindings));
	}

	@Nested
	class BindingHelpers {
		@Test
		void button() {
			final Button button = new Button("button", src);
			final Runnable method = mock(Runnable.class);
			final var adapter = bindings.bind(button, method);
			assertNotNull(adapter);
			bindings.accept(button);
			verify(method).run();
		}

		@Test
		void position() {
			final Position pos = new Position("pos", src);
			final Position.PositionHandler handler = mock(Position.PositionHandler.class);
			final var adapter = bindings.bind(pos, handler);
			assertNotNull(adapter);
			bindings.accept(pos.new PositionEvent(1, 2));
			verify(handler).handle(1, 2);
		}

		@Test
		void axis() {
			final Axis.AxisHandler handler = mock(Axis.AxisHandler.class);
			final var adapter = bindings.bind(axis, handler);
			assertNotNull(adapter);
			bindings.accept(axis.new AxisEvent(3));
			verify(handler).handle(3);
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
	void init() {
		bindings.bind(axis, handler);
		bindings.init();
		verify(src).bind(bindings);
	}

	@Test
	void accept() {
		final AxisEvent event = axis.new AxisEvent(42);
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
