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
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.control.Event.Handler;
import org.sarge.lib.util.Check;

/**
 * Set of bindings that map controller events to action handlers.
 * <p>
 * TODO
 * @author Sarge
 * @param <T> Action type
 */
public class Bindings<T> {
	private static final String DELIMITER = " ";

	/**
	 * Action entry.
	 */
	public class Action {
		private final T action;
		private final Event.Handler handler;
		private final List<Event.Key> keys = new ArrayList<>();

		/**
		 * Constructor.
		 * @param action		Action
		 * @param handler		Event handler
		 */
		private Action(T action, Handler handler) {
			this.action = notNull(action);
			this.handler = notNull(handler);
		}

		/**
		 * @return Action
		 */
		public T action() {
			return action;
		}

		/**
		 * @return Name of this action
		 */
		public String name() {
			return action.toString();
		}

		/**
		 * @return Event-handler for this action binding
		 */
		public Event.Handler handler() {
			return handler;
		}

		/**
		 * @return Event-keys bound to this action
		 */
		public Stream<Event.Key> keys() {
			return keys.stream();
		}

		/**
		 * Binds an event-key to this action.
		 * @param key Event-key
		 * @throws IllegalArgumentException if the key is already bound to another action
		 * @see Handler#category()
		 */
		public void bind(Event.Key key) {
			Check.notNull(key);
			if(bindings.containsKey(key)) throw new IllegalArgumentException("Key already bound: " + key);
			keys.add(key);
			bindings.put(key, this);
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this);
		}
	}

	private final List<Action> actions = new ArrayList<>();
	private final Map<Event.Key, Action> bindings = new HashMap<>();

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
			final Action action = bindings.get(event.key());
			if(action != null) {
				action.handler.handle(event);
			}
		};
	}

	/**
	 * Registers an action.
	 * @param action		Action
	 * @param handler		Handler
	 * @return New action entry
	 * @throws IllegalArgumentException if the action has already been added
	 */
	public Action add(T action, Event.Handler handler) {
		if(actions.stream().map(Action::action).anyMatch(action::equals)) {
			throw new IllegalArgumentException("Duplicate action: " + action);
		}

		final Action entry = new Action(action, handler);
		actions.add(entry);
		return entry;
	}

	/**
	 * Finds the action bound to the given key.
	 * @param key Event key
	 * @return Action
	 */
	public Optional<Action> find(Event.Key key) {
		return Optional.ofNullable(bindings.get(key));
	}

	/**
	 * Removes an existing binding.
	 * @param key Event-key to remove
	 * @return Action that the given key was previously bound to
	 * @throws IllegalArgumentException if there is no binding for the given key
	 */
	public Action remove(Event.Key key) {
		final Action action = find(key).orElseThrow(() -> new IllegalArgumentException("Key not bound: " + key));
		action.keys.remove(key);
		bindings.remove(key);
		return action;
	}

	/**
	 * Writes this set of bindings to the given writer.
	 * <p>
	 * Bindings have the following format: <tt>action event-key</tt>
	 * <p>
	 * where:
	 * <ul>
	 * <li><i>action</i> is the action name</li>
	 * <li><i>event-key</i> is the key of the bound event</li>
	 * </ul>
	 * <p>
	 * Notes:
	 * <ul>
	 * <li>A line is output for each key bound to an action and delimited by the line-separator</li>
	 * <li>The <i>action</i> name is returned by the {@link Action#toString()} method</li>
	 * <li>Binding entries are case-sensitive</li>
	 * </ul>
	 * @param out Output writer
	 * @see Event.Key#toString()
	 */
	public void write(PrintWriter out) {
		for(Action action : actions) {
			for(Event.Key key : action.keys) {
				out.print(action.action);
				out.print(DELIMITER);
				out.print(key);
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
	 * @see Event.Key#parse(String)
	 */
	public void read(Reader r) throws IOException {
		// Binding loader
		class Loader {
			private final Map<String, Action> map = actions.stream().collect(toMap(action -> action.action.toString(), Function.identity()));

			/**
			 * Loads a binding.
			 */
			private void load(String line) {
				// Tokenize binding
				final String[] tokens = StringUtils.split(line, DELIMITER);
				if(tokens.length > 2) throw new IllegalArgumentException("Invalid binding: " + line);

				// Lookup action
				final Action action = map.get(tokens[0]);
				if(action == null) throw new IllegalArgumentException("Unknown action: " + tokens[0]);

				// Parse event key
				final Event.Key key = Event.Key.parse(tokens[1]);

				// Bind event to action
				action.bind(key);
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

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
