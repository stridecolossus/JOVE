package org.sarge.jove.control;

import static java.util.stream.Collectors.toMap;

import java.util.*;
import java.util.function.Function;

import org.sarge.jove.control.Button.ButtonEvent;
import org.sarge.jove.control.Event.Handler;

/**
 * A set of <i>action bindings</i> maps events to actions.
 * @author Sarge
 */
public class ActionBindings implements Handler<Event> {
	/**
	 * A <i>binding</i> maps an event to an action.
	 */
	private record Binding<E extends Event>(Action<E> action, Object event) {
		/**
		 * Handles an event.
		 */
		@SuppressWarnings("unchecked")
		private void handle(Event event) {
			@SuppressWarnings("rawtypes")
			final Handler handler = action.handler();
			handler.handle(event);
		}
	}

	private final Map<Action<?>, List<Binding<?>>> actions;
	private final Map<Object, Binding<?>> bindings = new HashMap<>();

	/**
	 * Constructor.
	 * @param actions Actions
	 */
	public ActionBindings(Set<Action<?>> actions) {
		this.actions = actions.stream().collect(toMap(Function.identity(), _ -> new ArrayList<>()));
	}

	/**
	 * Copy constructor.
	 * @param actions Action bindings
	 */
	public ActionBindings(Map<Action<?>, List<Binding<?>>> actions) {
		this.actions = Map.copyOf(actions);
	}

	/**
	 * @return Action bindings
	 */
	public Map<Action<?>, List<Object>> actions() {
		return actions
				.keySet()
				.stream()
				.collect(toMap(Function.identity(), this::events));
	}

	/**
	 * Converts an action binding to its list of bound events.
	 */
	private List<Object> events(Action<?> action) {
		return actions
				.get(action)
				.stream()
				.map(Binding::event)
				.toList();
	}

	/**
	 * Looks up the action bound to the given event identifier.
	 * @param key Event key
	 * @return Action
	 */
	public Optional<Action<?>> action(Object id) {
		return Optional
				.ofNullable(bindings.get(id))
				.map(Binding::action);
	}

	/**
	 * Helper.
	 * Finds an action by name.
	 * @param name Action name
	 * @return Action
	 * @throws NoSuchElementException if the action is not present
	 */
	public Action<?> action(String name) {
		return actions
				.keySet()
				.stream()
				.filter(action -> action.name().equals(name))
				.findAny()
				.orElseThrow();
	}

	@Override
	public void handle(Event event) {
		// Determine event type
		final Object type = switch(event) {
			case ButtonEvent button -> button.button();
			default -> event.getClass();
		};

		// Lookup binding for this event (if any)
		final Binding<?> binding = bindings.get(type);
		if(binding == null) {
			return;
		}

		// Delegate to action
		binding.handle(event);
	}

	/**
	 * Binds an event to an action.
	 * @param <E> Event type
	 * @param action		Action
	 * @param binding		Event binding
	 * @throws IllegalArgumentException if the {@link #action} is not present
	 * @throws IllegalArgumentException if the event does not match {@link Action#type()}
	 * @throws IllegalStateException if the {@link #event} is already bound to an action
	 */
	public <E extends Event> void bind(Action<E> action, Object event) {
		// Check matching event type
		final Class<?> expected = switch(event) {
			case Button _ -> ButtonEvent.class;
			default -> event.getClass();
		};
		if(action.type() != expected) {
			throw new IllegalArgumentException("Mismatched event type %s for action %s".formatted(event, action));
		}

		// Lookup action bindings
		final List<Binding<?>> list = actions.get(action);
		if(list == null) {
			throw new IllegalArgumentException("Action not present: " + action);
		}

		// Check event is not already used
		final Binding<?> bound = bindings.get(event);
		if(Objects.nonNull(bound)) {
			throw new IllegalStateException("Event %s already bound to action %s".formatted(event, bound.action));
		}

		// Bind event to action
		final var binding = new Binding<>(action, event);
		list.add(binding);
		bindings.put(event, binding);
	}

	/**
	 * Helper.
	 * Binds an action by name.
	 * @param name		Action name
	 * @param event		Event
	 * @see #action(String)
	 * @see #bind(Action, Object)
	 */
	public void bind(String name, Object event) {
		bind(action(name), event);
	}

	/**
	 * Removes an event binding.
	 * @param <E> Event type
	 * @param id Event identifier
	 * @throws IllegalArgumentException if the event has not been bound
	 */
	public <E extends Event> void remove(Object id) {
		// Lookup binding
		final Binding<?> binding = bindings.remove(id);
		if(binding == null) {
			throw new IllegalArgumentException("Event not bound: " + id);
		}

		// Remove binding
		final List<Binding<?>> list = actions.get(binding.action);
		assert list.contains(binding);
		list.remove(binding);
	}

	/**
	 * Removes all bindings of the given action.
	 * @param action Action
	 * @throws IllegalArgumentException if the action is not present
	 */
	public void clear(Action<?> action) {
		// Lookup bindings for this action
		final List<Binding<?>> list = actions.get(action);
		if(list == null) {
			throw new IllegalArgumentException("Action not present: " + action);
		}

		// Remove event bindings
		for(Binding<?> b : list) {
			final Binding<?> prev = bindings.remove(b.event);
			assert Objects.nonNull(prev);
		}

		// Remove action bindings
		list.clear();
	}

	/**
	 * Removes <b>all</b> bindings.
	 */
	public void clear() {
		for(var list : actions.values()) {
			list.clear();
		}
		bindings.clear();
	}

	@Override
	public int hashCode() {
		return actions.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof ActionBindings that) &&
				this.actions.equals(that.actions);
	}
}
