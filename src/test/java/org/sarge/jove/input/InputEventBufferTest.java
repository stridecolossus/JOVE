package org.sarge.jove.input;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

public class InputEventBufferTest {
	private InputEventBuffer buffer;
	private InputEventHandler handler;
	private InputEvent event;

	@Before
	public void before() {
		handler = mock( InputEventHandler.class );
		buffer = new InputEventBuffer( handler );
		event = mock( InputEvent.class );
	}

	@Test
	public void constructor() {
		assertEquals( 0, buffer.getSize() );
	}

	@Test
	public void handle() {
		buffer.handle( event );
		assertEquals( 1, buffer.getSize() );
	}

	@Test
	public void execute() {
		buffer.handle( event );
		buffer.execute();
		verify( handler ).handle( event );
	}
}
