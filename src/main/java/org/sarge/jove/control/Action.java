package org.sarge.jove.control;

import java.util.*;
import java.util.function.Consumer;

/**
 * An <i>action</i> defines
 * TODO
 * @param <E> Event type
 * @author Sarge
 */
public interface Action<E extends Event> {
	/**
	 *
	 * @return
	 */
	String name();

	/**
	 *
	 * @param event
	 */
	void execute(E event);

	/**
	 * An <i>action bindings</i> maps events to actions.
	 */
	class Bindings implements Consumer<Event> {
		private final Map<Event, Runnable> handlers = new HashMap<>();
		private final Map<Action<?>, Set<Event>> bindings = new HashMap<>();

		/**
		 * @return Actions that can be bound
		 */
		public Set<Action<?>> actions() {
			return Set.copyOf(bindings.keySet());
		}

		/**
		 * Registers an action.
		 * @param action Action
		 */
		public void add(Action<?> action) {
			bindings.computeIfAbsent(action, __ -> new HashSet<>());
		}

		/**
		 * Maps the given action to its bindings.
		 */
		private Set<Event> map(Action<?> action) {
			final Set<Event> events = bindings.get(action);
			if(events == null) throw new IllegalStateException("Unknown action: " + action);
			return events;
		}

		/**
		 * Retrieves the bindings for the given action.
		 * @param action Action
		 * @return Bound events
		 * @throws IllegalStateException if the action has not been registered
		 */
		public Set<Event> bindings(Action<?> action) {
			return Set.copyOf(map(action));
		}

		/**
		 * Retrieves the action bound to the given event.
		 * @param event Event
		 * @return Bound action
		 */
		public Optional<Action<?>> action(Event event) {
			return Optional.ofNullable(find(event));
		}

		/**
		 * Finds the binding for a given event.
		 */
		private Action<?> find(Event event) {
			for(final var entry : bindings.entrySet()) {
				if(entry.getValue().contains(event)) {
					return entry.getKey();
				}
			}
			return null;
		}

		/**
		 * Binds an event.
		 * @param <E> Event type
		 * @param event		Event
		 * @param action	Action
		 * @throws IllegalStateException if the action has not been registered or the event is already bound to another action
		 */
		public <E extends Event> void bind(E event, Action<E> action) {
			if(find(event) != null) throw new IllegalStateException("Event is already bound: " + event);
			final Set<Event> events = map(action);
			events.add(event);
			handlers.put(event, () -> action.execute(event));
		}

		/**
		 * Removes a binding.
		 * @param event Bound event
		 * @throws IllegalStateException if the event is not bound
		 */
		public void remove(Event event) {
			final Action<?> action = find(event);
			if(action == null) throw new IllegalStateException("Unknown binding: " + event);
			bindings.get(action).remove(event);
			handlers.remove(event);
		}

		/**
		 * Removes all bindings for the given action.
		 * @param action Action to un-bind
		 * @throws IllegalStateException if the action has not been registered
		 */
		public void remove(Action<?> action) {
			final Set<Event> events = map(action);
			for(Event e : events) {
				handlers.remove(e);
			}
			events.clear();
		}

		@Override
		public void accept(Event event) {
			// Lookup action
			final Runnable handler = handlers.get(event.type());

			// Ignore if not bound
			if(handler == null) {
				return;
			}

			// TODO - match buttons

			// Delegate
			handler.run();
		}
	}
}
