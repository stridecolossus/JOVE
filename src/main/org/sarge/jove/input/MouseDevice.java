package org.sarge.jove.input;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import org.sarge.jove.common.Location;
import org.sarge.jove.common.MutableLocation;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Mouse device handler.
 * Maps AWT mouse events to generic {@link EventName}s.
 * @author Sarge
 */
public class MouseDevice extends MouseAdapter implements Device {
	private final InputEventHandler handler;

	private final MutableLocation loc = new MutableLocation();

	/**
	 * Constructor.
	 * @param handler AWT handler
	 */
	public MouseDevice( InputEventHandler handler ) {
		Check.notNull( handler );
		this.handler = handler;
	}

	@Override
	public void mousePressed( MouseEvent e ) {
		if( e.getClickCount() == 2 ) {
			mouseButton( EventType.DOUBLE_CLICK, e );
		}
		else {
			mouseButton( EventType.PRESS, e );
		}
	}

	@Override
	public void mouseReleased( MouseEvent e ) {
		mouseButton( EventType.RELEASE, e );
	}

	@Override
	public void mouseDragged( MouseEvent e ) {
		// Calc drag deltas
		final Location drag = new Location( e.getX() - loc.getX(), e.getY() - loc.getY() );

		// Update current position
		loc.set( e.getX(), e.getY() );

		// Generate drag event
		final String buttons = MouseEvent.getModifiersExText( e.getModifiersEx() );
		final EventName name = new EventName( EventType.DRAG, buttons );
		generate( name, drag, null );
	}

	@Override
	public void mouseWheelMoved( MouseWheelEvent e ) {
		final EventName name = new EventName( EventType.ZOOM, null );
		generate( name, null, e.getWheelRotation() );
	}

	/**
	 * Helper - generates a mouse-button event.
	 * @param type	Event type
	 * @param e		AWT mouse event
	 */
	private void mouseButton( EventType type, MouseEvent e ) {
		// Update mouse position
		loc.set( e.getX(), e.getY() );

		// Generate button event
		final String buttons = MouseEvent.getMouseModifiersText( e.getModifiers() );
		final EventName name = new EventName( type, buttons );
		generate( name, loc, null );
	}

	/**
	 * Delegates to the event handler.
	 */
	private void generate( EventName name, Location pos, Integer zoom ) {
		final InputEvent event = new InputEvent( this, name, pos, zoom );
		handler.handle( event );
	}

	@Override
	public String toString() {
		final ToString ts = new ToString( this );
		ts.append( loc );
		return ts.toString();
	}
}
