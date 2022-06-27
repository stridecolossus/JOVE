package org.sarge.jove.control;

import java.util.Set;
import java.util.function.Consumer;

/**
 * An <i>event</i> describes an input from the client.
 * @author Sarge
 */
public interface Event {
	/**
	 * @return Source that generated this event
	 */
	Source<?> source();

	/**
	 * An <i>event source</i> generates events and is the <i>binding point</i> for event handlers.
	 * @param <T> Event type
	 */
	interface Source<E extends Event> {
		/**
		 * @return Name of this source
		 */
		String name();

		/**
		 * Binds the given handler to this source.
		 * @param handler Event handler
		 * @return Underlying listener
		 */
		Object bind(Consumer<E> handler);
	}

	/**
	 * A <i>device</i> is comprised of a number of event sources.
	 */
	interface Device {
		/**
		 * @return Name of this device
		 */
		String name();

		/**
		 * @return Event sources
		 */
		Set<Source<?>> sources();
	}
}
