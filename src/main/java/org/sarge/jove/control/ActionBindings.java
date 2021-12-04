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
	 * Retrieves the events bound to the given handler.
	 * @param handler Event handler
	 * @return Events
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
	 * Binds an event handler to the given event.
	 * @param type			Event type
	 * @param handler		Event handler
	 * @throws IllegalArgumentException if the handler is already bound
	 */
	public <T extends Event> void bind(Object key, Consumer<? extends T> handler) {
		// Validate
		if(handler == this) throw new IllegalArgumentException("Cannot bind to self!");
		if(map.containsKey(handler)) throw new IllegalArgumentException("Handler is already bound: " + handler);

		// Lookup or create reverse mapping
		final var types = map.computeIfAbsent(handler, ignored -> new HashSet<>());

		// Add binding
		@SuppressWarnings("unchecked")
		final Consumer<Event> consumer = (Consumer<Event>) handler;
		bindings.put(key, consumer);
		types.add(key);
	}

	/**
	 * Convenience helper to bind an arbitrary adapter method to a button event.
	 * @param type			Button event type
	 * @param method		Method adapter
	 * @return Button handler
	 */
	public Consumer<Button> bind(Button button, Runnable method) {
		final Consumer<Button> handler = ignored -> method.run();
		bind(button, handler);
		return handler;
	}

	/**
	 * Convenience helper to bind a position adapter.
	 * @param type			Button event type
	 * @param adapter		Position adapter
	 * @return Position handler
	 */
	public Consumer<PositionEvent> bind(Source src, PositionEvent.Handler adapter) {
		final Consumer<PositionEvent> handler = pos -> adapter.handle(pos.x(), pos.y());
		bind(src, handler);
		src.bind(this);
		return handler;
	}

	/**
	 * Convenience helper to bind an axis adapter.
	 * @param type			Button event type
	 * @param adapter		Axis adapter
	 * @return Axis handler
	 */
	public Consumer<AxisEvent> bind(Axis axis, Axis.Handler adapter) {
		final Consumer<AxisEvent> handler = e -> adapter.handle(e.value());
		bind(axis, handler);
		axis.bind(this);
		return handler;
	}

	/**
	 * Removes a binding.
	 * @param type Event binding to remove
	 * @throws IllegalArgumentException if the event is not bound
	 */
	public void remove(Object key) {
		// Remove binding
		final Consumer<Event> handler = bindings.remove(key);
		if(handler == null) throw new IllegalArgumentException("Handler not bound: " + key);

		// Remove reverse mapping
		final var types = types(handler);
		types.remove(key);
	}

	/**
	 * Removes all bindings for the given handler.
	 * @param handler Action handler
	 * @throws IllegalArgumentException if the handler is not registered
	 */
	public void clear(Consumer<? extends Event> handler) {
		final var types = types(handler);
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

	// TODO - loader
//	public static class Loader implements ResourceLoaderWriter<BufferedReader, PrintWriter, ActionBindings> {
//		@Override
//		public BufferedReader map(InputStream in) throws IOException {
//			return new BufferedReader(new InputStreamReader(in));
//		}
//
//		@Override
//		public ActionBindings load(BufferedReader data) throws IOException {
//			return null;
//		}
//
//		@Override
//		public PrintWriter map(OutputStream out) throws IOException {
//			return null;
//		}
//
//		@Override
//		public void write(ActionBindings data, PrintWriter out) throws IOException {
//			for(final var entry : data.map.entrySet()) {
//				out.print(entry.getKey());
//				out.print(StringUtils.SPACE);
//				for(final var type : entry.getValue()) {
//					out.print(type.name());
//					out.print(StringUtils.SPACE);
//				}
//				out.println();
//			}
//		}
//	}

	// TODO - action class? composes handler and name for lookup when load bindings
	// TODO - also used as predicate for actions, e.g. button for any of mods/actions, etc.
}
