package org.sarge.jove.input;

import java.util.ArrayList;
import java.util.List;

import org.sarge.lib.util.Check;

/**
 * Buffered events handler.
 * @author Sarge
 */
public class InputEventBuffer implements InputEventHandler {
	private final List<InputEvent> events = new ArrayList<>();
	private final InputEventHandler handler;

	/**
	 * Constructor.
	 * @param handler Event handler
	 */
	public InputEventBuffer( InputEventHandler handler ) {
		Check.notNull( handler );
		this.handler = handler;
	}

	/**
	 * @return Number of buffered events
	 */
	public int getSize() {
		return events.size();
	}

	@Override
	public synchronized void handle( InputEvent event ) {
		events.add( event );
	}

	/**
	 * Executes all pending events.
	 */
	public synchronized void execute() {
		for( InputEvent e : events ) {
			handler.handle( e );
		}
		events.clear();
	}
}
