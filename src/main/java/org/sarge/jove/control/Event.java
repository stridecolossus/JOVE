package org.sarge.jove.control;

import static java.util.stream.Collectors.joining;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

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
	 */
	interface Source {
		/**
		 * Binds the given handler to this source.
		 * @param handler Event handler
		 */
		void bind(Consumer<Event> handler);
	}

	/**
	 * A <i>device</i> is comprised of a number of event sources.
	 */
	interface Device {
		/**
		 * @return Event sources
		 */
		Set<Source> sources();
	}

	/**
	 * Event name delimiter.
	 */
	String DELIMITER = "-";

	/**
	 * Builds a delimited event name from the given tokens.
	 * @param tokens Name tokens
	 * @return Event name
	 */
	static String name(String... tokens) {
		return Arrays
				.stream(tokens)
				.filter(Predicate.not(String::isEmpty))
				.collect(joining(DELIMITER));
	}
}
