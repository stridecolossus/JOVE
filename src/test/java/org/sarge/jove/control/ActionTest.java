package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.control.Action.Bindings;

public class ActionTest {
	private static final InputEvent.Type TYPE = InputEvent.Type.POSITION;

	private Action action;
	private Bindings bindings;

	@BeforeEach
	void before() {
		bindings = new Bindings();
		action = mock(Action.class);
		when(action.toString()).thenReturn("action");
	}

	@Test
	void constructor() {
		assertNotNull(bindings.actions());
		assertEquals(0, bindings.actions().count());
		assertEquals(Optional.empty(), bindings.binding(TYPE));
	}

	@Test
	void add() {
		bindings.add(action);
		assertArrayEquals(new Action[]{action}, bindings.actions().toArray());
		assertNotNull(bindings.bindings(action));
		assertEquals(0, bindings.bindings(action).count());
	}

	@Test
	void bindingsActionNotPresent() {
		assertThrows(IllegalArgumentException.class, () -> bindings.bindings(action));
	}

	@Test
	void bind() {
		bindings.bind(TYPE, action);
		assertArrayEquals(new InputEvent.Type[]{TYPE}, bindings.bindings(action).toArray());
		assertEquals(Optional.of(action), bindings.binding(TYPE));
	}

	@Test
	void bindAlreadyBound() {
		bindings.bind(TYPE, action);
		assertThrows(IllegalArgumentException.class, () -> bindings.bind(TYPE, action));
	}

	@Test
	void removeBinding() {
		bindings.bind(TYPE, action);
		bindings.remove(TYPE);
		assertEquals(0, bindings.bindings(action).count());
		assertEquals(Optional.empty(), bindings.binding(TYPE));
	}

	@Test
	void removeActionBindings() {
		bindings.bind(TYPE, action);
		bindings.remove(action);
		assertEquals(Optional.empty(), bindings.binding(TYPE));
	}

	@Test
	void removeActionBindingsNotPresent() {
		assertThrows(IllegalArgumentException.class, () -> bindings.remove(action));
	}

	@Test
	void clear() {
		bindings.bind(TYPE, action);
		bindings.clear();
		assertEquals(0, bindings.actions().count());
	}

	@Test
	void handle() {
		final InputEvent event = InputEvent.Type.POSITION.create(1, 2);
		bindings.bind(TYPE, action);
		bindings.handle(event);
		verify(action).execute(event);
	}

	@Test
	void handleIgnored() {
		final InputEvent event = InputEvent.Type.POSITION.create(1, 2);
		bindings.handle(event);
		verifyZeroInteractions(action);
	}

	@Test
	void write() {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		bindings.bind(TYPE, action);
		bindings.write(out);
		assertEquals("action Position", new String(out.toByteArray()).trim());
	}

	@Test
	void load() throws IOException {
		// Write bindings
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		bindings.bind(TYPE, action);
		bindings.write(out);

		// Read back
		final Bindings result = new Bindings();
		result.add(action);
		result.load(new ByteArrayInputStream(out.toByteArray()));
		assertEquals(bindings, result);
	}
}
