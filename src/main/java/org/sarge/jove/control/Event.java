package org.sarge.jove.control;

import java.util.Set;
import java.util.function.Consumer;

/**
 * An <i>event</i> describes an input from the client.
 * @author Sarge
 */
public interface Event {
//	/**
//	 * @return Type discriminator for this event
//	 */
//	Object type();

	/**
	 * @return Source that generated this event
	 */
	Source<?> source();

	/**
	 * An <i>event source</i> generates events and is the <i>binding point</i> for event handlers.
	 * @param <T> Event type
	 */
	interface Source<T extends Event> {
		/**
		 * @return Name of this source
		 */
		String name();

		/**
		 * Binds the given handler to this source.
		 * @param handler Event handler
		 */
		void bind(Consumer<T> handler);
	}

//	/**
//	 * Skeleton implementation.
//	 */
//	abstract class AbstractSource<T extends Event> implements Source<T> {
//		protected Consumer<T> handler;
//
//		@Override
//		public void bind(Consumer<T> handler) {
//			this.handler = handler;
//		}
//	}

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
