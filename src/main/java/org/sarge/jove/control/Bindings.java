package org.sarge.jove.control;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.sarge.jove.control.Event.Type;

/**
 * TODO
 * - bind() auto adds
 * @author Sarge
 */
public class Bindings implements Consumer<Event> {
	/**
	 * An <i>Entry</i> comprises the bindings for an event type.
	 */
	private static class Entry {
		private final Set<Consumer<? super Event>> handlers = new HashSet<>();

		/**
		 * Delegates the given event to all bound handlers.
		 * @param e Event
		 */
		private void accept(Event e) {
			for(final var c : handlers) {
				c.accept(e);
			}
		}

		/**
		 * Clears the bindings.
		 */
		private void clear() {
			handlers.clear();
		}
	}

	private final Map<Type<?>, Entry> bindings = new HashMap<>();
	private final Map<Object, Type<?>> reverse = new HashMap<>();

	/**
	 * @return Event types in this bindings
	 */
	public Stream<Type<?>> types() {
		return bindings.keySet().stream();
	}

	/**
	 * Helper - Retrieves a binding entry.
	 */
	private Entry entry(Type<?> type) {
		final Entry entry = bindings.get(type);
		if(entry == null) throw new IllegalArgumentException("Event type not registered: " + type);
		return entry;
	}

	/**
	 * Retrieves the bindings for the given event type.
	 * @param type Event type
	 * @return Bindings
	 * @throws IllegalArgumentException if the event type is not registered
	 */
	public Stream<Consumer<? super Event>> bindings(Type<?> type) {
		final Entry entry = entry(type);
		return entry.handlers.stream();
	}

	/**
	 * Retrieves the event type bound to the given handler.
	 * @param handler Event handler
	 * @return Event type
	 */
	public Optional<Type<?>> binding(Consumer<Event> handler) {
		return Optional.ofNullable(reverse.get(handler));
	}

	/**
	 * Registers an event type.
	 * @param type Event type
	 * @throws IllegalArgumentException for a duplicate event type
	 */
	public void add(Type<?> type) {
		if(bindings.containsKey(type)) throw new IllegalArgumentException("Duplicate event type: " + type);
		bindings.put(type, new Entry());
	}

	/**
	 * Binds an event handler to the given event type.
	 * @param type			Event type
	 * @param handler		Event handler
	 * @throws IllegalArgumentException if the handler is already bound
	 */
	public <T extends Event> void bind(Type<T> type, Consumer<? super T> handler) {
		// Validate
		if(handler == this) throw new IllegalArgumentException("Cannot bind to this bindings");
		if(reverse.containsKey(handler)) throw new IllegalArgumentException("Handler is already bound: " + handler);

		// Lookup or create bindings
		final Entry entry = bindings.computeIfAbsent(type, ignored -> new Entry());

		// Add binding
		@SuppressWarnings("unchecked")
		final var consumer = (Consumer<? super Event>) handler;
		entry.handlers.add(consumer);
		reverse.put(handler, type);
	}

	/**
	 * Convenience helper to bind an arbitrary method to a button event.
	 * @param type			Button event type
	 * @param method		Method
	 * @return Event handler adapter
	 */
	public Consumer<Button> bind(Type<Button> type, Runnable method) {
		final Consumer<Button> adapter = ignored -> method.run();
		bind(type, adapter);
		return adapter;
	}

	// TODO
	// - bind(source, handler) => also auto binds source -> handler
	// - ditto for bind type? (if not already bound?)
	// - unknown handler?
	// - cascading? e.g. button-press-shift, then button-press, then button, then ALL?
	// - persistence

	/**
	 * Removes a binding.
	 * @param handler Event handler to remove
	 * @throws IllegalArgumentException if the handler is not bound
	 */
	public void remove(Consumer<Event> handler) {
		// Lookup reverse binding
		final Type<?> type = reverse.remove(handler);
		if(type == null) throw new IllegalArgumentException("Handler not bound: " + handler);

		// Remove binding
		final Entry entry = bindings.get(type);
		entry.handlers.remove(handler);
	}

	/**
	 * Removes all bindings for the given event type.
	 * @param type Event type
	 * @throws IllegalArgumentException if the event type is not registered
	 */
	public void clear(Type<?> type) {
		final Entry entry = entry(type);
		entry.clear();
	}

	/**
	 * Clears <b>all</b> bindings.
	 * Note that the registered event types are unchanged.
	 */
	public void clear() {
		for(Entry entry : bindings.values()) {
			entry.clear();
		}
		reverse.clear();
	}

	@Override
	public void accept(Event e) {
		final Entry entry = bindings.get(e.type());
		if(entry != null) {
			entry.accept(e);
		}
	}
}
