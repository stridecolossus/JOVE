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
	private Event.Descriptor descriptor;

	@BeforeEach
	public void before() {
		bindings = new Bindings<>();
		descriptor = new Event.Descriptor(Event.Category.BUTTON, 1, Event.Operation.PRESS);
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
		// Create bindings handler
		final Event.Handler wrapper = bindings.handler();
		assertNotNull(wrapper);

		// Register an action
		final var action = addAction();
		action.bind(descriptor);

		// Generate an event and check delegated to action
		final Event event = new Event(descriptor);
		wrapper.handle(event);
		verify(handler).handle(event);
	}

	@Test
	public void add() {
		final var action = addAction();
		assertEquals(1, bindings.actions().count());
		assertEquals(action, bindings.actions().iterator().next());
		assertNotNull(action);
		assertEquals(ACTION, action.action());
		assertEquals(handler, action.handler());
		assertNotNull(action.events());
		assertEquals(0, action.events().count());
	}

	@Test
	public void addAlreadyAdded() {
		addAction();
		assertThrows(IllegalArgumentException.class, () -> bindings.add(ACTION, handler));
	}

	@Test
	public void find() {
		final var action = addAction();
		action.bind(descriptor);
		assertEquals(Optional.of(action), bindings.find(descriptor));
	}

	@Test
	public void findNotBound() {
		assertEquals(Optional.empty(), bindings.find(descriptor));
	}

	@Test
	public void bind() {
		final var action = addAction();
		action.bind(descriptor);
		assertEquals(1, action.events().count());
		assertEquals(descriptor, action.events().iterator().next());
	}

	@Test
	public void bindAlreadyBound() {
		final var action = addAction();
		action.bind(descriptor);
		assertThrows(IllegalArgumentException.class, () -> action.bind(descriptor));
	}

	@Test
	public void remove() {
		final var action = addAction();
		action.bind(descriptor);
		bindings.remove(descriptor);
		assertEquals(0, action.events().count());
		assertEquals(Optional.empty(), bindings.find(descriptor));
	}

	@Test
	public void removeNotBound() {
		assertThrows(IllegalArgumentException.class, () -> bindings.remove(descriptor));
	}

	@Test
	public void write() {
		// Add an action with a couple of bindings
		final var action = addAction();
		action.bind(descriptor);
		action.bind(new Event.Descriptor(Event.Category.BUTTON, 2, Event.Operation.RELEASE));

		// Add another action and binding
		final Event.Handler other = mock(Event.Handler.class);
		bindings.add("other", other).bind(Event.Descriptor.ZOOM);

		// Output bindings
		final StringWriter out = new StringWriter();
		bindings.write(new PrintWriter(out));

		// Check output
		final String[] expected = {
			"action BUTTON-PRESS-1",
			"action BUTTON-RELEASE-2",
			"other ZOOM",
			"",
		};
		assertEquals(String.join(System.lineSeparator(), expected), out.toString());
	}

	@Test
	public void read() throws IOException {
		final var action = addAction();
		bindings.read(new StringReader("action BUTTON-PRESS-1"));
		assertArrayEquals(new Event.Descriptor[]{descriptor}, action.events().toArray());
	}

	@Test
	public void readUnknownAction() throws IOException {
		assertThrows(IllegalArgumentException.class, () -> bindings.read(new StringReader("cobblers BUTTON-PRESS-999")));
	}

	@Test
	public void readInvalidBinding() throws IOException {
		assertThrows(IllegalArgumentException.class, () -> bindings.read(new StringReader("cobblers")));
	}
}
