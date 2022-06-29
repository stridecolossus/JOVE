package org.sarge.jove.control;

import static java.util.stream.Collectors.joining;

import java.util.*;
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

	/**
	 * Event name delimiter.
	 */
	String DELIMITER = "-";

	/**
	 * Builds a human-readable, hyphen-delimited name from the given tokens.
	 * @param tokens Tokens
	 * @return Event name
	 */
	static String name(Object... tokens) {
		return Arrays
				.stream(tokens)
				.map(String::valueOf)
				.collect(joining(DELIMITER));
	}
}
