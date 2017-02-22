package org.sarge.jove.input;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

public class ActionBindingsTest {
	private static final String NAME = "name";
	
	private ActionBindings bindings;
	private Action action;
	private EventKey key;

	@Before
	public void before() {
		action = mock(Action.class);
		key = new EventKey(EventType.PRESS, "key");
		bindings = new ActionBindings();
	}

	@Test
	public void bind() {
		bindings.add(NAME, action);
		bindings.bind(key, NAME);
		assertEquals(Optional.of(action), bindings.getAction(key));
	}

	@Test
	public void bindingNotFound() {
		assertEquals(Optional.empty(), bindings.getAction(key));
	}

	@Test(expected = IllegalArgumentException.class)
	public void bindInvalidAction() {
		bindings.bind(key, NAME);
	}

	@Test
	public void handle() {
		// Bind an action
		bindings.bind(key, NAME, action);

		// Create an event
		final Device dev = mock(Device.class);
		final InputEvent event = new InputEvent(dev, key);
		bindings.handle(event);

		// Check action invoked
		verify(action).execute(event);
	}

	@Test
	public void clear() {
		bindings.bind(key, NAME, action);
		bindings.clear();
		assertEquals(Optional.empty(), bindings.getAction(key));
	}

	@Test
	public void persist() throws IOException {
		// Bind an action
		bindings.bind(key, NAME, action);

		// Persist bindings
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		bindings.save(out);
		assertEquals(true, out.toString().trim().endsWith("PRESS+key=name"));

		// Re-load bindings and check restored
		bindings.clear();
		bindings.load(new ByteArrayInputStream(out.toByteArray()));
		assertEquals(Optional.of(action), bindings.getAction(key));
	}
}
