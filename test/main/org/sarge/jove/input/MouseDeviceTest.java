package org.sarge.jove.input;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.Component;
import java.awt.event.MouseEvent;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.common.Location;

public class MouseDeviceTest {
	private InputEventHandler handler;
	private MouseDevice dev;

	@Before
	public void before() {
		handler = mock( InputEventHandler.class );
		dev = new MouseDevice( handler );
	}

	private static MouseEvent createEvent( int id ) {
		final Component src = mock( Component.class );
		when( src.getLocationOnScreen() ).thenReturn( new java.awt.Point() );
		return new MouseEvent( src, id, 0L, MouseEvent.BUTTON1_DOWN_MASK, 1, 2, 1, false, 1 );
	}

	@Test
	public void press() {
		final MouseEvent event = createEvent( MouseEvent.MOUSE_PRESSED );
		dev.mousePressed( event );
		final InputEvent expected = new InputEvent( dev, new EventName( EventType.PRESS, "Button1" ), new Location( 1, 2 ), null );
		verify( handler ).handle( expected );
	}

	@Test
	public void drag() {
		final MouseEvent event = createEvent( MouseEvent.MOUSE_DRAGGED );
		dev.mouseDragged( event );
		final InputEvent expected = new InputEvent( dev, new EventName( EventType.DRAG, "Button1" ), new Location( 1, 2 ), null );
		verify( handler ).handle( expected );
	}
}
