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
import org.junit.jupiter.api.Test;
import org.sarge.jove.control.Event.Source;
import org.sarge.jove.control.Event.Type;

public class BindingsTest {
	private Bindings bindings;
	private Type type;
	private Consumer<Event> handler;

	@BeforeEach
	void before() {
		bindings = new Bindings();
		type = mock(Type.class);
		handler = mock(Consumer.class);
	}

	@Test
	void constructor() {
		assertNotNull(bindings.types());
		assertEquals(0, bindings.types().count());
		assertEquals(Optional.empty(), bindings.binding(handler));
	}

	@Test
	void add() {
		bindings.add(type);
		assertArrayEquals(new Type[]{type}, bindings.types().toArray());
		assertNotNull(bindings.bindings(type));
		assertEquals(0, bindings.bindings(type).count());
	}

	@Test
	void addDuplicate() {
		bindings.add(type);
		assertThrows(IllegalArgumentException.class, () -> bindings.add(type));
	}

	@Test
	void bind() {
		bindings.bind(type, handler);
		assertArrayEquals(new Object[]{handler}, bindings.bindings(type).toArray());
		assertEquals(Optional.of(type), bindings.binding(handler));
	}

	@Test
	void bindButtonMethod() {
		final Button button = new Button("button", mock(Source.class));
		final Runnable method = mock(Runnable.class);
		bindings.bind(button, method);
		bindings.accept(button);
		verify(method).run();
	}

	@Test
	void bindNotAdded() {
		assertThrows(IllegalArgumentException.class, () -> bindings.bindings(type));
	}

	@Test
	void bindDuplicateListener() {
		bindings.bind(type, handler);
		assertThrows(IllegalArgumentException.class, () -> bindings.bind(type, handler));
	}

	@Test
	void bindSelf() {
		assertThrows(IllegalArgumentException.class, () -> bindings.bind(type, bindings));
	}

	@Test
	void remove() {
		bindings.bind(type, handler);
		bindings.remove(handler);
		assertEquals(0, bindings.bindings(type).count());
	}

	@Test
	void removeNotBound() {
		assertThrows(IllegalArgumentException.class, () -> bindings.remove(handler));
	}

	@Test
	void clear() {
		bindings.bind(type, handler);
		bindings.clear(type);
		assertEquals(0, bindings.bindings(type).count());
	}

	@Test
	void clearNotBound() {
		assertThrows(IllegalArgumentException.class, () -> bindings.clear(type));
	}

	@Test
	void clearAll() {
		bindings.bind(type, handler);
		bindings.clear();
		assertArrayEquals(new Type[]{type}, bindings.types().toArray());
		assertEquals(0, bindings.bindings(type).count());
	}

	@Test
	void accept() {
		final Event event = mock(Event.class);
		when(event.type()).thenReturn(type);
		bindings.bind(type, handler);
		bindings.accept(event);
		verify(handler).accept(event);
	}

	@Test
	void acceptUnknownEvent() {
		bindings.bind(type, handler);
		bindings.accept(mock(Event.class));
		verifyNoMoreInteractions(handler);
	}
}
