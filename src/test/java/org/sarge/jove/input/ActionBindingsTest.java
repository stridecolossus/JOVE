package org.sarge.jove.input;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

public class ActionBindingsTest {
	private ActionBindings bindings;
	private Action action;
	private EventKey key;

	@Before
	public void before() {
		action = mock( Action.class );
		when( action.getName() ).thenReturn( "name" );
		key = new EventKey( EventType.PRESS, "press" );
		bindings = new ActionBindings();
	}

	@Test
	public void bind() {
		bindings.add( action );
		bindings.bind( key, "name" );
		verify( action ).getName();
		assertEquals( action, bindings.getAction( key ) );
	}

	@Test
	public void bindingNotFound() {
		assertEquals( null, bindings.getAction( key ) );
	}

	@Test(expected=IllegalArgumentException.class)
	public void bindInvalidAction() {
		bindings.bind( key, "name" );
	}

	@Test
	public void handle() {
		// Bind an action
		bindings.add( action );
		bindings.bind( key, "name" );

		// Create an event
		final InputEvent event = new InputEvent( mock( Device.class ), key, null, null );
		bindings.handle( event );

		// Check action invoked
		verify( action ).getName();
		verify( action ).execute( event );

		// Check event re-pooled
		assertEquals( 1, InputEvent.POOL.getSize() );

		// Un-bind action
		bindings.clear();
		bindings.handle( event );
		verifyNoMoreInteractions( action );
	}

	@Test
	public void clear() {
		bindings.add( action );
		bindings.bind( key, "name" );
		bindings.clear();
		assertEquals( null, bindings.getAction( key ) );
	}

	@Test
	public void persist() {
		// Bind an action
		bindings.add( action );
		bindings.bind( key, "name" );

		// Persist bindings
		final Properties props = new Properties();
		bindings.save( props );

		// Check saved bindings
		assertEquals( 1, props.size() );
		assertEquals( "name", props.get( EventType.PRESS.name() + "+press" ) );
	}

	@Test
	public void load() throws IOException {
		// Bind an action
		bindings.add( action );
		bindings.bind( key, "name" );

		// Persist bindings
		final Properties props = new Properties();
		bindings.save( props );

		// Load and check bindings restored
		bindings.clear();
		bindings.load( props );
		assertEquals( action, bindings.getAction( key ) );
	}
}
