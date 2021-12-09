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
import org.sarge.jove.control.Axis.Handler;
import org.sarge.jove.control.DefaultButton.Action;
import org.sarge.jove.control.Event.Source;
import org.sarge.lib.util.Check;

/**
 * An <i>action bindings</i> is a mutable set of mappings that bind input events to <i>action</i> handlers.
 * <p>
 * This class provides convenience <i>bind</i> variants to bind events to common handler methods, e.g. {@link #bind(Axis, Handler)} to bind axis events.
 * <p>
 * Example:
 * <pre>
 * void handle(float value) { ... }
 * Axis axis = ...
 * ActionBindings bindings = new ActionBindings();
 * bindings.bind(axis, this::handle);</pre>
 * <p>
 * Action handlers can be registered using the {@link #add(Consumer)} to initialise actions that initially have no bindings.
 * Note that handlers are also automatically registered by the various bind methods.
 * <p>
 * Notes:
 * <ul>
 * <li>The binding key is the {@link Event#type()} of the event</li>
 * <li>Multiple events can be bound to a single action</li>
 * <li>Events that are not bound are ignored</li>
 * <li>The <i>action bindings</i> class is itself an action handler</li>
 * <li>Unmatched modified button events are delegated to the default button binding (if present), see {@link #bind(Button, Runnable)}</li>
 * </ul>
 * <p>
 * @see Event
 * @author Sarge
 */
public class ActionBindings implements Consumer<Event> {
	private final Map<Object, Consumer<Event>> bindings = new HashMap<>();
	private final Map<Consumer<? extends Event>, Set<Object>> handlers = new HashMap<>();

	/**
	 * Helper - Retrieves the reverse mapping of the event types bound to the given handler.
	 */
	private Set<Object> types(Object handler) {
		final Set<Object> types = handlers.get(handler);
		if(types == null) throw new IllegalArgumentException("Handler not registered: " + handler);
		return types;
	}

	/**
	 * @return Action handlers
	 */
	public Stream<Consumer<? extends Event>> handlers() {
		return handlers.keySet().stream();
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
		if(handlers.containsKey(handler)) throw new IllegalArgumentException("Duplicate handler: " + handler);
		handlers.put(handler, new HashSet<>());
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
		Check.notNull(type);
		Check.notNull(handler);
		if(handler == this) throw new IllegalArgumentException("Cannot bind to self!");
		if(bindings.containsKey(type)) throw new IllegalArgumentException("Event type is already bound: " + type);

		// Lookup or create reverse mapping
		final Set<Object> types = handlers.computeIfAbsent(handler, ignored -> new HashSet<>());

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
	 * Binds a button to events matching the button template.
	 * @param button		Button template
	 * @param handler		Handler
	 */
	private void bindButton(Button button, Consumer<Button> handler) {
		final Consumer<Button> wrapper = event -> {
			if(button.matches(event)) {
				handler.accept(event);
			}
		};
		bindLocal(button.type(), wrapper);
	}

	/**
	 * Binds a button to an action handler.
	 * <p>
	 * Note that for an unmatched <i>modified</i> button this class delegates to the matching <i>default</i> button (if present).
	 * <p>
	 * For example, given these bindings:
	 * <pre>
	 * new DefaultButton("key", Action.PRESS)
	 * new ModifiedButton("key", Action.PRESS, IntegerEnumeration.ALT.value())</pre>
	 * </pre>
	 * The following event:
	 * <pre>
	 * new ModifiedButton("key", Action.PRESS, ...)}</pre>
	 * is delegated to the first binding if the second is not matched, i.e. by stripping the keyboard modifiers.
	 * <p>
	 * @param button		Button template
	 * @param handler		Event handler
	 */
	public void bind(Button button, Runnable handler) {
		final Consumer<Button> adapter = ignored -> handler.run();
		bindButton(button, adapter);
	}

	/**
	 * Binds a button to a toggle handler.
	 * @param button		Button template
	 * @param handler		Toggle handler
	 * @see #bind(Button, Runnable)
	 */
	public void bind(Button button, Button.ToggleHandler handler) {
		final Consumer<Button> adapter = event -> handler.handle(event.action() == Action.PRESS);
		bindButton(button, adapter);
	}

	/**
	 * Binds a position event to an action handler.
	 * @param src			Position event source
	 * @param handler		Event handler
	 */
	public void bind(Source<PositionEvent> src, PositionEvent.Handler handler) {
		final Consumer<PositionEvent> adapter = pos -> handler.handle(pos.x(), pos.y());
		bind(src, adapter);
	}

	/**
	 * Binds an axis to an action handler.
	 * @param axis			Axis
	 * @param handler		Event handler
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
	 * Note that the set of registered action handlers is unchanged.
	 */
	public void clear() {
		bindings.clear();
		handlers.values().forEach(Set::clear);
	}

	@Override
	public void accept(Event e) {
		final Consumer<Event> handler = bindings.get(e.type());
		if(handler == null) {
			if(e instanceof ModifiedButton mod) {
				accept(mod);
			}
		}
		else {
			handler.accept(e);
		}
	}

	/**
	 * Handles a modified button by trying to delegate to the default button.
	 */
	private void accept(ModifiedButton button) {
		final Button def = new DefaultButton(button.id(), button.action());
		final Consumer<Event> handler = bindings.get(def.type());
		if(handler == null) {
			return;
		}
		handler.accept(def);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("handlers", handlers.size())
				.append("bindings", bindings.size())
				.build();
	}
}
