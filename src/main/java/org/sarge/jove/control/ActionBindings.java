package org.sarge.jove.control;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.control.Axis.AxisEvent;
import org.sarge.jove.control.Event.Source;

/**
 * A <i>bindings</i> is a mutable set of mappings that bind an input event to an <i>action</i> (an event consumer).
 * <p>
 * This class provides convenience _bind_ variants to bind events to common handler methods, e.g. {@link #bind(Axis, org.sarge.jove.control.Axis.Handler)} to bind axis events.
 * <p>
 * Example:
 * <pre>
 * void handle(float value) { ... }
 * Axis axis = ...
 * ActionBindings bindings = new ActionBindings();
 * bindings.bind(axis, this::handle);
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
public class ActionBindings implements Consumer<Event> {
	private final Map<Object, Consumer<Event>> bindings = new HashMap<>();
	private final Map<Consumer<? extends Event>, Set<Object>> map = new HashMap<>();

	/**
	 * Helper - Retrieves the reverse mapping of the event types bound to the given handler.
	 */
	private Set<Object> types(Object handler) {
		final var types = map.get(handler);
		if(types == null) throw new IllegalArgumentException("Handler not registered: " + handler);
		return types;
	}

	/**
	 * @return Action handlers
	 */
	public Stream<Consumer<? extends Event>> handlers() {
		return map.keySet().stream();
	}

	/**
	 * Retrieves the bound action handler for the given event.
	 * @param type Event type
	 * @return Action handler
	 */
	public Optional<Consumer<? extends Event>> binding(Object type) {
		return Optional.ofNullable(bindings.get(type));
	}

	/**
	 * Retrieves the event types bound to the given handler.
	 * @param handler Event handler
	 * @return Event types
	 * @throws IllegalArgumentException if the handler is not bound
	 */
	public Stream<Object> bindings(Consumer<? extends Event> handler) {
		final var types = types(handler);
		return types.stream();
	}

	/**
	 * Registers an action handler.
	 * @param handler Action handler
	 * @throws IllegalArgumentException for a duplicate handler
	 */
	public void add(Consumer<? extends Event> handler) {
		if(map.containsKey(handler)) throw new IllegalArgumentException("Duplicate handler: " + handler);
		map.put(handler, new HashSet<>());
	}

	/**
	 * Binds an event type to an action handler.
	 * @param <T> Event type
	 * @param type			Event type
	 * @param handler		Event handler
	 * @throws IllegalArgumentException if the handler is already bound
	 */
	private <T extends Event> void bindLocal(Object type, Consumer<? extends T> handler) {
		// Validate
		if(handler == this) throw new IllegalArgumentException("Cannot bind to self!");
		if(map.containsKey(handler)) throw new IllegalArgumentException("Handler is already bound: " + handler);

		// Lookup or create reverse mapping
		final Set<Object> types = map.computeIfAbsent(handler, ignored -> new HashSet<>());

		// Add binding
		@SuppressWarnings("unchecked")
		final Consumer<Event> consumer = (Consumer<Event>) handler;
		bindings.put(type, consumer);
		types.add(type);
	}

	/**
	 * Binds an event source to an action handler.
	 * Note this method also automatically binds the event source to this set of bindings.
	 * @param <T> Event type
	 * @param src			Event source
	 * @param handler		Handler
	 */
	public <T extends Event> void bind(Source<T> src, Consumer<T> handler) {
		bindLocal(src, handler);
		src.bind(this);
	}

	/**
	 * Convenience helper to bind a button to a handler method.
	 * @param type			Button
	 * @param handler		Event handler
	 */
	public void bind(Button button, Runnable handler) {
		final Consumer<Button> adapter = ignored -> handler.run();
		bindLocal(button.type(), adapter);
	}

	/**
	 * Convenience helper to bind a position event.
	 * @param src			Position event source
	 * @param handler		Event handler
	 */
	public void bind(Source<PositionEvent> src, PositionEvent.Handler handler) {
		final Consumer<PositionEvent> adapter = pos -> handler.handle(pos.x(), pos.y());
		bind(src, adapter);
	}

	/**
	 * Convenience helper to bind an axis event.
	 * @param axis			Axis
	 * @param adapter		Event handler
	 */
	public void bind(Axis axis, Axis.Handler handler) {
		final Consumer<AxisEvent> adapter = e -> handler.handle(e.value());
		bind(axis, adapter);
	}

	/**
	 * Removes a binding.
	 * @param type Event binding to remove
	 * @throws IllegalArgumentException if the event is not bound
	 */
	public void remove(Object type) {
		// Remove binding
		final Consumer<Event> handler = bindings.remove(type);
		if(handler == null) throw new IllegalArgumentException("Handler not bound: " + type);

		// Remove reverse mapping
		final Set<Object> types = types(handler);
		types.remove(type);
	}

	/**
	 * Removes all bindings for the given handler.
	 * @param handler Action handler
	 * @throws IllegalArgumentException if the handler is not registered
	 */
	public void clear(Consumer<? extends Event> handler) {
		final Set<Object> types = types(handler);
		types.forEach(bindings::remove);
		types.clear();
	}

	/**
	 * Clears <b>all</b> bindings.
	 * Note that the registered handler are unchanged.
	 */
	public void clear() {
		bindings.clear();
		map.values().forEach(Set::clear);
	}

	@Override
	public void accept(Event e) {
		final Consumer<Event> handler = bindings.get(e.type());
		if(handler == null) {
			return;
		}
		handler.accept(e);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("handlers", map.size())
				.append("bindings", bindings.size())
				.build();
	}
}
