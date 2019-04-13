package org.sarge.jove.control;

import static java.util.stream.Collectors.toMap;
import static org.sarge.lib.util.Check.notNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.sarge.jove.control.Event.Handler;
import org.sarge.lib.util.AbstractObject;
import org.sarge.lib.util.Check;

/**
 * A <i>bindings</i> maps controller events to action handlers.
 * <p>
 * Usage:
 * <pre>
 * // Create bindings
 * final Bindings bindings = new Bindings<>();
 *
 * // Register actions
 * final Event.Handler handler = ...
 * final Bindings.Action action = bindings.add(handler);
 * ...
 *
 * // Bind an event to the action
 * final Event.Descriptor event = new Event.Descriptor(Event.Category.BUTTON, Event.Type.PRESS, "Space");
 * action.bind(event);
 *
 * // Remove a binding
 * action.remove(event);
 *
 * // Remove all bindings on an action
 * action.clear();
 *
 * // Delegate an event to a bound action
 * final Event event = ...
 * final Optional<Action> action = bindings.find(event.descriptor());
 * action.handler().ifPresent(handler -> handler.handle(event));
 *
 * // Or more simply
 * final Event.Handler handler = bindings.handler();
 * handler.handle(event);
 * </pre>
 * Notes:
 * <ul>
 * <li>the bindings class is not thread-safe</li>
 * <li>it is assumed that event handlers have {@link Event.Handler#equals(Object)} and {@link Event.Handler#toString()} correctly implemented</li>
 * </ul>
 * @author Sarge
 */
public class Bindings extends AbstractObject {
	private static final String DELIMITER = " ";

	/**
	 * Binding entry.
	 */
	public class Action extends AbstractObject {
		private final Event.Handler handler;
		private final List<Event.Descriptor> events = new ArrayList<>();

		/**
		 * Constructor.
		 * @param action Action
		 */
		private Action(Handler handler) {
			this.handler = notNull(handler);
		}

		/**
		 * @return Event-handler for this action binding
		 */
		public Event.Handler handler() {
			return handler;
		}

		/**
		 * @return Events bound to this action
		 */
		public Stream<Event.Descriptor> events() {
			return events.stream();
		}

		/**
		 * Binds an event to this action.
		 * @param descriptor Event descriptor
		 * @throws IllegalArgumentException if the event is already bound to another action
		 */
		public void bind(Event.Descriptor descriptor) {
			Check.notNull(descriptor);
			if(bindings.containsKey(descriptor)) throw new IllegalArgumentException("Event already bound to this action: " + descriptor);
			events.add(descriptor);
			bindings.put(descriptor, this);
		}

		/**
		 * Removes an existing binding.
		 * @param descriptor Event to remove
		 * @return Action that the given event was previously bound to
		 * @throws IllegalArgumentException if there is no binding for the given event
		 */
		public Action remove(Event.Descriptor descriptor) {
			if(!bindings.containsKey(descriptor)) throw new IllegalArgumentException("Event not bound: " + descriptor);
			events.remove(descriptor);
			return bindings.remove(descriptor);
		}

		/**
		 * Clears all events bound to this action.
		 */
		public void clear() {
			events.stream().forEach(bindings::remove);
			events.clear();
		}
	}

	private final List<Action> actions = new ArrayList<>();
	private final Map<Event.Descriptor, Action> bindings = new HashMap<>();

	/**
	 * @return Actions
	 */
	public Stream<Action> actions() {
		return actions.stream();
	}

	/**
	 * Creates an event handler that delegates to this set of bindings.
	 * @return Bindings event handler
	 */
	public Event.Handler handler() {
		return event -> {
			final Action action = bindings.get(event.descriptor());
			if(action != null) {
				action.handler.handle(event);
			}
		};
	}

	/**
	 * Registers an action.
	 * @param handler Event handler
	 * @return New action entry
	 * @throws IllegalArgumentException if the action has already been added
	 */
	public Action add(Event.Handler handler) {
		if(actions.stream().map(Action::handler).anyMatch(handler::equals)) {
			throw new IllegalArgumentException("Duplicate action handler: " + handler);
		}

		final Action entry = new Action(handler);
		actions.add(entry);
		return entry;
	}

	/**
	 * Finds the action bound to the given event.
	 * @param descriptor Event descriptor
	 * @return Action
	 */
	public Optional<Action> find(Event.Descriptor descriptor) {
		return Optional.ofNullable(bindings.get(descriptor));
	}

	/**
	 * Writes this set of bindings to the given writer.
	 * <p>
	 * Bindings have the following format: <tt>action event-descriptor</tt>
	 * <p>
	 * where:
	 * <ul>
	 * <li><i>action</i> is the action name</li>
	 * <li><i>event-descriptor</i> is the string representation of the bound event</li>
	 * </ul>
	 * <p>
	 * Notes:
	 * <ul>
	 * <li>A line is output for each binding and delimited by the line-separator</li>
	 * <li>The <i>action</i> name is returned by the {@link Action#toString()} method</li>
	 * <li>Binding entries are case-sensitive</li>
	 * </ul>
	 * @param out Output writer
	 * @see Event.Descriptor#toString()
	 */
	public void write(PrintWriter out) {
		for(Action action : actions) {
			for(Event.Descriptor e : action.events) {
				out.print(action.handler);
				out.print(DELIMITER);
				out.print(e);
				out.println();
			}
		}
	}

	/**
	 * Loads a set of bindings from the given reader.
	 * @param r Reader
	 * @throws IOException if the bindings cannot be loaded
	 * @throws IllegalArgumentException if a binding is not valid or an action cannot be found in this set of bindings
	 * @see #write(PrintWriter)
	 * @see Event.Descriptor#parse(String)
	 */
	public void read(Reader r) throws IOException {
		// Binding loader
		class Loader {
			private final Map<String, Action> map = actions.stream().collect(toMap(action -> action.handler.toString(), Function.identity()));

			/**
			 * Loads a binding.
			 */
			private void load(String line) {
				// Tokenize binding
				final String[] tokens = StringUtils.split(line, DELIMITER);
				if(tokens.length > 2) throw new IllegalArgumentException("Invalid binding: " + line);

				// Lookup action
				final Action action = map.get(tokens[0]);
				if(action == null) throw new IllegalArgumentException("Unknown action handler: " + tokens[0]);

				// Parse event
				final Event.Descriptor descriptor = Event.Descriptor.parse(tokens[1]);

				// Bind event to action
				action.bind(descriptor);
			}
		}

		// Load bindings
		final Loader loader = new Loader();
		try(final BufferedReader in = new BufferedReader(r)) {
			in.lines()
				.map(String::trim)
				.filter(Predicate.not(String::isEmpty))
				.forEach(loader::load);
		}
	}
}
