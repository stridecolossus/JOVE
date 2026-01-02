package org.sarge.jove.control;

import static java.util.Objects.requireNonNull;
import static org.sarge.jove.util.Validation.requireNotEmpty;

import org.sarge.jove.control.Event.Handler;

/**
 * An <i>action</i> specifies a handler for a type of event.
 * @param <E> Event type
 * @author Sarge
 */
public record Action<E extends Event>(String name, Class<E> type, Handler<E> handler) {
	/**
	 * Constructor.
	 * @param name			Action identifier
	 * @param type			Event type
	 * @param handler		Event handler
	 */
	public Action {
		requireNotEmpty(name);
		requireNonNull(type);
		requireNonNull(handler);
	}
}
