package org.sarge.jove.control;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.control.Axis.AxisEvent;
import org.sarge.jove.control.Event.Type;
import org.sarge.jove.control.Position.PositionEvent;
import org.sarge.jove.util.ResourceLoader;

/**
 * A <i>bindings</i> is a mutable set of mappings that bind an input event to an <i>action</i> (an event consumer).
 * <p>
 * Usage:
 * <pre>
 * 	// Define action bindings
 * 	Button key = new Button(...);
 * 	Consumer<Button> action = ...
 * 	Bindings bindings = new Bindings();
 * 	bindings.bind(key, action);
 *
 *	// Register bindings with event source
 * 	Device dev = ...
 * 	Source src = dev.sources().get(...);
 * 	src.bind(bindings);
 * </pre>
 * <p>
 * The bindings class provides convenience methods to bind events to common handler methods, e.g. {@link #bind(Type, org.sarge.jove.control.Axis.AxisHandler)} to bind an axis handler.
 * <p>
 * Examples:
 * <pre>
 * 	// Bind a keyboard button to a runnable
 * 	Button key = new Button(...);
 * 	Runnable action = ...
 * 	bindings.bind(key, action);
 *
 * 	// Or to a method
 * 	class Target {
 * 		void key()
 * 		void pointer(float x, float y)
 * 	}
 * 	Target target = new Target();
 * 	bindings.bind(key, target::key);
 *
 * 	// Bind mouse pointer to X-Y handlers
 * 	Position ptr = ...
 * 	Position.Handler handler = (x, y) -> ...
 * 	bindings.bind(ptr, handler);
 * 	bindings.bind(ptr, target::pointer);
 * </pre>
 * <p>
 * Notes:
 * <ul>
 * <li>The <i>bindings</i> is itself an event consumer</li>
 * <li>Events that are not bound are ignored</li>
 * <li>Multiple events can be bound to a single action</li>
 * </ul>
 * <p>
 * @see Event
 * @author Sarge
 */
public class Bindings implements Consumer<Event> {
	/**
	 * An <i>entry</i> is comprised of the event types bound to an action handler.
	 */
	private static class Entry {
		private final Set<Type<?>> types = new HashSet<>();

		private void clear() {
			types.clear();
		}
	}

	private final Map<Type<?>, Consumer<Event>> bindings = new HashMap<>();
	private final Map<Consumer<? extends Event>, Entry> map = new HashMap<>();

	/**
	 * @return Action handlers
	 */
	public Stream<Consumer<? extends Event>> handlers() {
		return map.keySet().stream();
	}

	/**
	 * Helper - Retrieves the entry for the given handler.
	 */
	private Entry entry(Consumer<? extends Event> handler) {
		final Entry entry = map.get(handler);
		if(entry == null) throw new IllegalArgumentException("Handler not registered: " + handler);
		return entry;
	}

	/**
	 * Retrieves the bound action handler for the given event.
	 * @param type Event type
	 * @return Action handler
	 */
	public Optional<Consumer<? extends Event>> binding(Type<?> type) {
		return Optional.ofNullable(bindings.get(type));
	}

	/**
	 * Retrieves the events bound to the given handler.
	 * @param handler Event handler
	 * @return Events
	 * @throws IllegalArgumentException if the handler is not bound
	 */
	public Stream<Type<?>> bindings(Consumer<? extends Event> handler) {
		final Entry entry = entry(handler);
		return entry.types.stream();
	}

	/**
	 * Registers an action handler.
	 * @param handler Action handler
	 * @throws IllegalArgumentException for a duplicate handler
	 */
	public void add(Consumer<? extends Event> handler) {
		if(map.containsKey(handler)) throw new IllegalArgumentException("Duplicate handler: " + handler);
		map.put(handler, new Entry());
	}

	/**
	 * Binds an event handler to the given event.
	 * @param type			Event type
	 * @param handler		Event handler
	 * @throws IllegalArgumentException if the handler is already bound
	 */
	public <T extends Event> void bind(Type<T> type, Consumer<? extends T> handler) {
		// Validate
		if(handler == this) throw new IllegalArgumentException("Cannot bind to this bindings");
		if(map.containsKey(handler)) throw new IllegalArgumentException("Handler is already bound: " + handler);

		// Lookup or create bindings
		final Entry entry = map.computeIfAbsent(handler, ignored -> new Entry());

		// Add binding
		@SuppressWarnings("unchecked")
		final Consumer<Event> consumer = (Consumer<Event>) handler;
		bindings.put(type, consumer);
		entry.types.add(type);
	}

	/**
	 * Convenience helper to bind an arbitrary adapter method to a button event.
	 * @param type			Button event type
	 * @param method		Method adapter
	 * @return Button handler
	 */
	public Consumer<Button> bind(Type<Button> type, Runnable method) {
		final Consumer<Button> handler = ignored -> method.run();
		bind(type, handler);
		return handler;
	}

	/**
	 * Convenience helper to bind a position adapter.
	 * @param type			Button event type
	 * @param adapter		Position adapter
	 * @return Position handler
	 */
	public Consumer<PositionEvent> bind(Type<PositionEvent> type, Position.PositionHandler adapter) {
		final Consumer<PositionEvent> handler = e -> adapter.handle(e.x, e.y);
		bind(type, handler);
		return handler;
	}

	/**
	 * Convenience helper to bind an axis adapter.
	 * @param type			Button event type
	 * @param adapter		Axis adapter
	 * @return Axis handler
	 */
	public Consumer<AxisEvent> bind(Type<AxisEvent> type, Axis.AxisHandler adapter) {
		final Consumer<AxisEvent> handler = e -> adapter.handle(e.value());
		bind(type, handler);
		return handler;
	}

	/**
	 * Removes a binding.
	 * @param type Event binding to remove
	 * @throws IllegalArgumentException if the event is not bound
	 */
	public void remove(Type<?> type) {
		// Remove binding
		final Consumer<? extends Event> handler = bindings.remove(type);
		if(type == null) throw new IllegalArgumentException("Handler not bound: " + handler);

		// Remove reverse mapping
		final Entry entry = entry(handler);
		entry.types.remove(type);
	}

	/**
	 * Removes all bindings for the given handler.
	 * @param handler Action handler
	 * @throws IllegalArgumentException if the handler is not registered
	 */
	public void clear(Consumer<? extends Event> handler) {
		final Entry entry = entry(handler);
		entry.types.forEach(bindings::remove);
		entry.clear();
	}

	/**
	 * Clears <b>all</b> bindings.
	 * Note that the registered handler are unchanged.
	 */
	public void clear() {
		bindings.clear();
		map.values().forEach(Entry::clear);
	}

	@Override
	public void accept(Event e) {
		final Consumer<Event> handler = bindings.get(e.type());
		if(handler != null) {
			handler.accept(e);
		}
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("handlers", map.size())
				.append("bindings", bindings.size())
				.build();
	}

	/**
	 * Loader for action bindings.
	 */
	public class Loader implements ResourceLoader<BufferedReader, Bindings> {
		private Loader() {
		}

		@Override
		public BufferedReader map(InputStream in) throws IOException {
			return new BufferedReader(new InputStreamReader(in));
		}

		@Override
		public Bindings load(BufferedReader data) throws IOException {
			// TODO
			return Bindings.this;
		}

		/**
		 * Writes this set of bindings.
		 * @param out Writer
		 */
		public void write(PrintWriter out) {
			// TODO
		}
	}

	/**
	 * @return New loader for this set of bindings
	 */
	public Loader loader() {
		return new Loader();
	}
}
