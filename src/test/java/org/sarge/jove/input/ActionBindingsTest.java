package org.sarge.jove.input;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

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
		key = EventKey.POOL.get();
		key.init( EventType.PRESS, "press" );
		final InputEvent event = InputEvent.POOL.get();
		event.init( mock( Device.class ), key );
		bindings.handle( event );

		// Check action invoked
		verify( action ).getName();
		verify( action ).execute( event );

		// Check event re-pooled
		assertEquals( 1, InputEvent.POOL.getSize() );
		assertEquals( 1, EventKey.POOL.getSize() );

		// Un-bind action
		EventKey.POOL.get();
		InputEvent.POOL.get();
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
	public void persist() throws IOException {
		// Bind an action
		bindings.add( action );
		bindings.bind( key, "name" );

		// Persist bindings
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		bindings.save( out );
		assertEquals( true, out.toString().trim().endsWith( "PRESS+press=name" ) );

		// Re-load bindings and check restored
		bindings.clear();
		bindings.load( new ByteArrayInputStream( out.toByteArray() ) );
		assertEquals( action, bindings.getAction( key ) );
	}
}
