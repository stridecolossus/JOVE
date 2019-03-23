package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BindingsTest {
	private static final String ACTION = "action";

	private Bindings<String> bindings;
	private Event.Handler handler;
	private Event.Key key;

	@BeforeEach
	public void before() {
		bindings = new Bindings<>();
		key = Event.Key.of(Event.Category.BUTTON, Event.Type.PRESS, 42);
		handler = mock(Event.Handler.class);
	}

	@Test
	public void constructor() {
		assertNotNull(bindings.actions());
		assertEquals(0, bindings.actions().count());
	}

	private Bindings<String>.Action addAction() {
		return bindings.add(ACTION, handler);
	}

	@Test
	public void handler() {
		final Event.Handler wrapper = bindings.handler();
		assertNotNull(wrapper);
		final var action = addAction();
		action.bind(key);
		wrapper.handle(key.event());
		verify(handler).handle(key.event());
	}

	@Test
	public void add() {
		final var action = addAction();
		assertEquals(1, bindings.actions().count());
		assertEquals(action, bindings.actions().iterator().next());
		assertNotNull(action);
		assertEquals(ACTION, action.action());
		assertEquals(handler, action.handler());
		assertNotNull(action.keys());
		assertEquals(0, action.keys().count());
	}

	@Test
	public void addAlreadyAdded() {
		addAction();
		assertThrows(IllegalArgumentException.class, () -> bindings.add(ACTION, handler));
	}

	@Test
	public void find() {
		final var action = addAction();
		action.bind(key);
		assertEquals(Optional.of(action), bindings.find(key));
	}

	@Test
	public void findNotBound() {
		assertEquals(Optional.empty(), bindings.find(key));
	}

	@Test
	public void bind() {
		final var action = addAction();
		action.bind(key);
		assertEquals(1, action.keys().count());
		assertEquals(key, action.keys().iterator().next());
	}

	@Test
	public void bindAlreadyBound() {
		final var action = addAction();
		action.bind(key);
		assertThrows(IllegalArgumentException.class, () -> action.bind(key));
	}

	@Test
	public void remove() {
		final var action = addAction();
		action.bind(key);
		bindings.remove(key);
		assertEquals(0, action.keys().count());
		assertEquals(Optional.empty(), bindings.find(key));
	}

	@Test
	public void removeNotBound() {
		assertThrows(IllegalArgumentException.class, () -> bindings.remove(key));
	}

	@Test
	public void write() {
		// Add an action with a couple of bindings
		final var action = addAction();
		action.bind(key);
		action.bind(Event.Key.of(Event.Category.BUTTON, Event.Type.RELEASE, 2));

		// Add another action and binding
		final Event.Handler other = mock(Event.Handler.class);
		bindings.add("other", other).bind(Event.Key.ZOOM);

		// Output bindings
		final StringWriter out = new StringWriter();
		bindings.write(new PrintWriter(out));

		// Check output
		final String[] expected = {
			"action BUTTON-PRESS-42",
			"action BUTTON-RELEASE-2",
			"other ZOOM",
			"",
		};
		assertEquals(String.join(System.lineSeparator(), expected), out.toString());
	}

	@Test
	public void read() throws IOException {
		final var action = addAction();
		bindings.read(new StringReader("action BUTTON-PRESS-42"));
		assertArrayEquals(new Event.Key[]{key}, action.keys().toArray());
	}

	@Test
	public void readUnknownAction() throws IOException {
		assertThrows(IllegalArgumentException.class, () -> bindings.read(new StringReader("cobblers BUTTON-PRESS-42")));
	}

	@Test
	public void readInvalidBinding() throws IOException {
		assertThrows(IllegalArgumentException.class, () -> bindings.read(new StringReader("cobblers")));
	}
}
