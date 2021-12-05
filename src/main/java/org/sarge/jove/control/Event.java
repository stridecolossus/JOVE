package org.sarge.jove.control;

import java.util.Set;
import java.util.function.Consumer;

/**
 * An <i>event</i> describes an input from the client.
 * @author Sarge
 */
public interface Event {
	/**
	 * @return Type discriminator for this event
	 */
	Object type();

	/**
	 * An <i>event source</i> generates events and is a <i>binding point</i> for an event handler.
	 * @param <T> Event type
	 */
	interface Source<T extends Event> {
		/**
		 * Binds the given handler to this source.
		 * @param handler Event handler
		 */
		void bind(Consumer<Event> handler);
	}

	/**
	 * Skeleton implementation.
	 */
	abstract class AbstractSource<T extends Event> implements Source<T> {
		protected Consumer<Event> handler;

		@Override
		public void bind(Consumer<Event> handler) {
			this.handler = handler;
		}
	}

	/**
	 * A <i>device</i> is comprised of a number of event sources.
	 */
	interface Device {
		/**
		 * @return Event sources
		 */
		Set<Source<?>> sources();
	}
}
