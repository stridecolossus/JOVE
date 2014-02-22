package org.sarge.jove.input;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.Util;

/**
 * Keyboard device.
 * TODO - will only work for desktop (AWT!)
 * @author Sarge
 */
public class KeyboardDevice extends KeyAdapter implements Device {
	private final InputEventHandler handler;

	/**
	 * Constructor.
	 * @param handler Event handler
	 */
	public KeyboardDevice( InputEventHandler handler ) {
		Check.notNull( handler );
		this.handler = handler;
	}

	@Override
	public void keyPressed( KeyEvent e ) {
		generate( EventType.PRESS, e );
	}

	@Override
	public void keyReleased( KeyEvent e ) {
		generate( EventType.RELEASE, e );
	}

	/**
	 * Generates an input device for the given key event and delegates to the handler.
	 */
	private void generate( EventType type, KeyEvent e ) {
		// Get key and modifiers text
		final int key = e.getKeyCode();
		final int mods = e.getModifiers();
		final String keyText = clean( KeyEvent.getKeyText( key ) );
		final String modsText = clean( KeyEvent.getKeyModifiersText( mods ) );

		// Build event name
		final String name;
		if( Util.isEmpty( modsText ) ) {
			name = keyText;
		}
		else {
			name = modsText + "+" + keyText;
		}

		// Create event and delegate to handler
		final InputEvent event = new InputEvent( this, new EventName( type, name ), null, null );
		handler.handle( event );
	}

	/**
	 * Strips white-space.
	 * Event names are used as keys when being persisted.
	 */
	private static String clean( String str ) {
		final StringBuilder sb = new StringBuilder();
		for( char ch : str.toCharArray() ) {
			if( Character.isWhitespace( ch ) ) continue;
			sb.append( ch );
		}
		return sb.toString();
	}
}
