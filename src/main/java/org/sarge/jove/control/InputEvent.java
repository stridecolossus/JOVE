package org.sarge.jove.control;

import static java.util.stream.Collectors.joining;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.sarge.jove.util.Check;

/**
 * An <i>input event</i> describes an event generated by a device.
 * @author Sarge
 * @see Device
 */
public interface InputEvent {
	/**
	 * @return Type of this event
	 */
	Type type();

	/**
	 * An <i>event type</i> is the descriptor for an input event.
	 */
	interface Type {
		/**
		 * @return Event type name
		 */
		String name();

		/**
		 * Parses an instance of this event type from the given string.
		 * @param tokens String tokens
		 * @return New event type
		 */
		Type parse(String[] tokens);
	}

	interface Handler {
		void handle(InputEvent event);
	}

	/**
	 * Partial implementation.
	 * <p>
	 * The constructor accepts an arbitrary number of arguments that are used to construct the name of this event type and its hash value.
	 * Generally these will be the class members that uniquely identify an instance of the event.
	 * <p>
	 * The name is delimited by the slash-character, for example the name of a sub-class with the arguments <code>"Axis", 42</code> would be <code>Axis-42</code>.
	 * <p>
	 * The {@link #parse(String)} static factory method is used to parse an event type from a given string.
	 * The <b>first</b> argument in the constructor is used to lookup the sub-class {@link #parse(String[])} implementation for a given event type (<code>Axis</code> in the above example).
	 */
	abstract class AbstractInputEventType implements Type {
		/**
		 * Name delimiter.
		 */
		protected static final String DELIMITER = "-";

		private static final Map<Object, Type> REGISTRY = new HashMap<>();

		/**
		 * Parse an input event type from the given string representation.
		 * @param str String
		 * @return Event type
		 * @see Type#parse(String[])
		 * @throws UnsupportedOperationException for an unknown event type
		 */
		public static Type parse(String str) {
			final String[] tokens = str.split(DELIMITER);
			final Type type = REGISTRY.get(tokens[0]);
			if(type == null) throw new UnsupportedOperationException("Unknown event type: " + tokens[0]);
			return type.parse(tokens);
		}

		private final String name;
		private final transient int hash;

		/**
		 * Constructor.
		 * @param args Input event class members
		 */
		protected AbstractInputEventType(Object... args) {
			Check.notEmpty(args);
			this.name = Arrays.stream(args).map(Object::toString).peek(this::validate).collect(joining(DELIMITER));
			this.hash = Objects.hash(args);
			REGISTRY.put(args[0], this);
		}

		private void validate(String token) {
			if(StringUtils.isEmpty(token)) throw new IllegalArgumentException("Event name cannot be empty");
			if(token.contains(StringUtils.SPACE)) throw new IllegalArgumentException("Event name cannot contain spaces");
			if(token.contains(DELIMITER)) throw new IllegalArgumentException("Event name cannot contain a slash delimiter");
		}

		@Override
		public final String name() {
			return name;
		}

		@Override
		public final int hashCode() {
			return hash;
		}

		@Override
		public abstract boolean equals(Object obj);

		@Override
		public String toString() {
			return name();
		}
	}
}
