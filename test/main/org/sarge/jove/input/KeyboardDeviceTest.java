package org.sarge.jove.input;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.awt.Component;
import java.awt.event.KeyEvent;

import org.junit.Before;
import org.junit.Test;

public class KeyboardDeviceTest {
	private KeyboardDevice dev;
	private InputEventHandler handler;
	private Component src;

	@Before
	public void before() {
		src = mock( Component.class );
		handler = mock( InputEventHandler.class );
		dev = new KeyboardDevice( handler );
	}

	private KeyEvent createKeyEvent( int id, int mods ) {
		return new KeyEvent( src, id, 0L, mods, KeyEvent.VK_A, 'A' );
	}

	@Test
	public void pressed() {
		final KeyEvent awtEvent = createKeyEvent( KeyEvent.KEY_PRESSED, 0 );
		dev.keyPressed( awtEvent );
		final EventName expected = new EventName( EventType.PRESS, "A" );
		final InputEvent event = new InputEvent( dev, expected, null, null );
		verify( handler ).handle( event );
	}

	@Test
	public void pressedModifiers() {
		final KeyEvent awtEvent = createKeyEvent( KeyEvent.KEY_PRESSED, KeyEvent.SHIFT_DOWN_MASK + KeyEvent.CTRL_DOWN_MASK );
		dev.keyPressed( awtEvent );
		final EventName expected = new EventName( EventType.PRESS, "Ctrl+Shift+A" );
		final InputEvent event = new InputEvent( dev, expected, null, null );
		verify( handler ).handle( event );
	}

	@Test
	public void released() {
		final KeyEvent awtEvent = createKeyEvent( KeyEvent.KEY_RELEASED, 0 );
		dev.keyReleased( awtEvent );
		final EventName expected = new EventName( EventType.RELEASE, "A" );
		final InputEvent event = new InputEvent( dev, expected, null, null );
		verify( handler ).handle( event );
	}
}
