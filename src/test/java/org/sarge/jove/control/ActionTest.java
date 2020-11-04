package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.control.Action.Bindings;
import org.sarge.jove.control.Button.Operation;

public class ActionTest {
	private Button button;
	private InputEvent<Button> event;
	private Action action;
	private Bindings bindings;

	@BeforeEach
	void before() {
		// Create an event
		button = new Button("name");
		event = button.event(Operation.PRESS);

		// Create an action
		action = mock(Action.class);
		when(action.toString()).thenReturn("action");

		// Create bindings
		bindings = new Bindings();
	}

	@Test
	void constructor() {
		assertNotNull(bindings.actions());
		assertEquals(0, bindings.actions().count());
		assertEquals(Optional.empty(), bindings.binding(button));
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
		bindings.bind(button, action);
		assertArrayEquals(new InputEvent.Type[]{button}, bindings.bindings(action).toArray());
		assertEquals(Optional.of(action), bindings.binding(button));
	}

	@Test
	void bindAlreadyBound() {
		bindings.bind(button, action);
		assertThrows(IllegalStateException.class, () -> bindings.bind(button, action));
	}

	@Test
	void removeBinding() {
		bindings.bind(button, action);
		bindings.remove(button);
		assertEquals(0, bindings.bindings(action).count());
		assertEquals(Optional.empty(), bindings.binding(button));
	}

	@Test
	void removeActionBindings() {
		bindings.bind(button, action);
		bindings.remove(action);
		assertEquals(Optional.empty(), bindings.binding(button));
	}

	@Test
	void removeActionBindingsNotPresent() {
		assertThrows(IllegalArgumentException.class, () -> bindings.remove(action));
	}

	@Test
	void clear() {
		bindings.bind(button, action);
		bindings.clear();
		assertEquals(0, bindings.actions().count());
	}

	@Test
	void accept() {
		bindings.bind(button, action);
		bindings.accept(event);
		verify(action).accept(event);
	}

	@Test
	void acceptRunnable() {
		final Runnable runnable = mock(Runnable.class);
		bindings.bind(button, runnable);
		bindings.accept(event);
		verify(runnable).run();
	}

	@Test
	void acceptIgnored() {
		bindings.accept(event);
		verifyZeroInteractions(action);
	}

	@Test
	void write() {
		final StringWriter out = new StringWriter();
		bindings.bind(button, action);
		bindings.write(out);
		assertEquals("action org.sarge.jove.control.Button-name", out.toString().trim());
	}

	@Test
	void load() throws IOException {
		// Write bindings
		final StringWriter out = new StringWriter();
		bindings.bind(button, action);
		bindings.write(out);

		// Read back
		final Bindings result = new Bindings();
		result.add(action);
		result.load(new StringReader(out.toString()));
		assertEquals(Optional.of(action), result.binding(button));
	}
}
