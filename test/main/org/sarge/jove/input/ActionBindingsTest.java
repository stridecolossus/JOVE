package org.sarge.jove.input;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

public class ActionBindingsTest {
	// Using explicit mock rather than Mockito to verify the save/load methods
	public static class MockAction implements Action {
		public boolean done;

		@Override
		public void execute( InputEvent event ) {
			done = true;
		}
	}

	private ActionBindings bindings;
	private EventName key;
	private MockAction action;
	private Device dev;

	@Before
	public void before() {
		bindings = new ActionBindings();
		key = new EventName( EventType.PRESS, "name" );
		action = new MockAction();
		dev = mock( Device.class );
	}

	@Test
	public void add() {
		// Add a binding and check handler can be retrieved
		bindings.add( key, action );
		assertEquals( action, bindings.getAction( key ) );

		// Check reverse binding
		assertNotNull( bindings.getEvents( action ) );
		assertEquals( true, bindings.getEvents( action ).contains( key ) );
	}

	@Test
	public void remove() {
		bindings.add( key, action );
		bindings.remove( key );
		assertEquals( null, bindings.getAction( key ) );
		assertEquals( true, bindings.getEvents( action ).isEmpty() );
	}

	@Test
	public void clear() {
		bindings.add( key, action );
		bindings.remove( key );
		assertEquals( null, bindings.getAction( key ) );
	}

	@Test
	public void handle() {
		bindings.add( key, action );
		final InputEvent ev = new InputEvent( dev, key, null, null );
		bindings.handle( ev );
		assertEquals( true, action.done );
	}

	@Test
	public void save() {
		// Add a binding
		bindings.add( key, action );

		// Persist bindings
		final Properties props = new Properties();
		bindings.save( props );

		// Verify entry added
		final String str = key.toString();
		assertEquals( true, props.containsKey( str ) );
		assertEquals( action.getClass().getName(), props.get( str ) );
	}

	@Test
	public void load() throws IOException {
		// Add a binding
		bindings.add( key, action );

		// Persist bindings
		final Properties props = new Properties();
		bindings.save( props );

		// Load new bindings
		final ActionBindings restored = new ActionBindings();
		restored.load( props );

		// Check contains binding
		final Action result = bindings.getAction( key );
		assertNotNull( result );
		assertEquals( MockAction.class, result.getClass() );
	}
}
