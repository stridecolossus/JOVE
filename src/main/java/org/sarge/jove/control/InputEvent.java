package org.sarge.jove.control;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.sarge.jove.control.InputEvent.Type;

/**
 * An <i>input event</i> describes an event generated by a device.
 * @author Sarge
 */
public interface InputEvent<T extends Type> {
	/**
	 * @return Type of this event
	 */
	T type();

	/**
	 * An <i>event type</i> is the descriptor for an input event.
	 */
	interface Type {
		/**
		 * Name delimiter.
		 */
		String DELIMITER = "-";

		/**
		 * @return Event name qualifier
		 */
		String name();

		/**
		 * The <i>event type parser</i> instantiates an event-type from its string representation.
		 * TODO
		 */
		class Parser {
			private final Map<String, Method> registry = new HashMap<>();

			/**
			 * Parses an event-type from its string representation.
			 * @param str String representation of the event
			 * @return Parsed event-type
			 * @throws RuntimeException if the event-type cannot be parsed
			 */
			public Type parse(String str) {
				// Find classname delimiter
				final int idx = str.indexOf(' ');
				if((idx < 1) || (idx == str.length())) throw new IllegalArgumentException(String.format("Invalid event-type representation: [%s]", str));

				// Lookup constructor
				final Method parse = registry.computeIfAbsent(str.substring(0, idx), Parser::register);

				// Create event type
				try {
					final String rep = str.substring(idx + 1);
					return (Type) parse.invoke(null, new Object[]{rep});
				}
				catch(Exception e) {
					throw new RuntimeException(String.format("Error instantiating event-type: [%s]", str), e);
				}
			}

			/**
			 * Looks up the parse method for the given event-type.
			 * @param name Event-type class name
			 * @return Parse method
			 * @throws IllegalArgumentException if the event type is unknown or the parse method cannot be found
			 */
			private static Method register(String name) {
				try {
					// Lookup class
					final Class<?> clazz = Class.forName(name);
					if(!Type.class.isAssignableFrom(clazz)) throw new IllegalArgumentException("Not an event class: " + name);

					// Lookup parse method
					final Method method = clazz.getDeclaredMethod("parse", String.class);
					if(!Type.class.isAssignableFrom(method.getReturnType())) throw new IllegalArgumentException("Invalid return type for parse method: " + name);
					return method;
				}
				catch(ClassNotFoundException e) {
					throw new IllegalArgumentException("Unknown event type: " + name, e);
				}
				catch(NoSuchMethodException e) {
					throw new IllegalArgumentException("Cannot find parse method: " + name, e);
				}
				catch(IllegalArgumentException e) {
					throw e;
				}
				catch(Exception e) {
					throw new RuntimeException("Error looking up parse method: " + name, e);
				}
			}
		}
	}

	/**
	 * An <i>input event source</i> defines a generator of input events.
	 * @param <T> Type of event
	 */
	interface Source<T extends Type> {
		/**
		 * @return Events generated by this source
		 */
		List<T> events();

		/**
		 * Enables generation of events.
		 * @param handler Event handler
		 */
		void enable(Consumer<InputEvent<T>> handler);

		/**
		 * Disables event generation.
		 */
		void disable();
	}

	/**
	 * A <i>device</i> is comprised of a group of event sources.
	 */
	interface Device {
		/**
		 * @return Device name
		 */
		String name();

		/**
		 * @return Event sources for this device
		 */
		Set<Source<?>> sources();
	}
}
