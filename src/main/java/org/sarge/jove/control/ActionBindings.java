package org.sarge.jove.control;
import static java.util.stream.Collectors.toMap;

import java.util.*;
import java.util.function.Function;

/**
 * A set of <i>action bindings</i> maps events to actions.
 * @author Sarge
 */
public class ActionBindings {
	private final Map<Action<?>, List<Device<?>>> bindings;
	private final Map<Device<?>, Action<?>> inverse = new HashMap<>();

	/**
	 * Constructor.
	 * @param actions Actions
	 */
	public ActionBindings(List<Action<?>> actions) {
		this.bindings = actions.stream().collect(toMap(Function.identity(), _ -> new ArrayList<>()));
	}

	/**
	 * Retrieves the bindings for the given action.
	 * @param action Action
	 * @return Bindings
	 * @throws IllegalArgumentException if the action is not present
	 */
	private List<Device<?>> list(Action<?> action) {
		final var list = bindings.get(action);
		if(list == null) {
			throw new IllegalArgumentException("Action not present: " + action);
		}
		return list;
	}

	/**
	 * @return Actions managed by this set of bindings
	 */
	public Set<Action<?>> actions() {
		return bindings.keySet();
	}

	/**
	 * Enumerates the bindings for the given action.
	 * @param <E> Event type
	 * @param action Action
	 * @return Bindings
	 * @throws IllegalArgumentException if the action is not present
	 */
	@SuppressWarnings("unchecked")
	public <E extends Event> List<Device<E>> bindings(Action<E> action) {
		@SuppressWarnings("rawtypes")
		final List list = list(action);
		return Collections.unmodifiableList(list);
	}

	/**
	 * Retrieves the action bound to the given device.
	 * @param <E> Event type
	 * @param device Device
	 * @return Bound action
	 */
	public <E extends Event> Optional<Action<E>> action(Device<E> device) {
		@SuppressWarnings("unchecked")
		final var action = (Action<E>) inverse.get(device);
		return Optional.ofNullable(action);
	}

	/**
	 * Binds a device to the given action.
	 * @param <E> Event type
	 * @param action Action
	 * @param device Device to bind
	 * @throws IllegalArgumentException if the action is not present
	 * @throws IllegalStateException if the device is already bound to an action
	 */
	public <E extends Event> void bind(Action<E> action, Device<E> device) {
		if(inverse.containsKey(device)) {
			throw new IllegalStateException("Device already bound: " + device);
		}

		// Register binding
		final var list = list(action);
		list.add(device);
		inverse.put(device, action);

		// Bind device to action
		final var handler = action.handler();
		device.bind(handler);
	}

	/**
	 * Removes a binding.
	 * @param <E> Event type
	 * @param action Action
	 * @param device Bound device
	 * @throws IllegalArgumentException if the action or binding is not present
	 */
	public <E extends Event> void remove(Action<E> action, Device<E> device) {
		if(!inverse.containsKey(device)) {
			throw new IllegalArgumentException("Binding not present: %s -> %s".formatted(device, action));
		}

		// Unbind device
		device.remove();

		// Remove binding
		final var list = list(action);
		list.remove(device);

		// Remove inverse binding
		final var prev = inverse.remove(device);
		assert prev == action;
	}

	/**
	 * Removes all bindings for the given action.
	 * @param action Action
	 */
	public void clear(Action<?> action) {
		final var list = list(action);
		for(var device : list) {
			device.remove();
			inverse.remove(device);
		}
		list.clear();
	}

	/**
	 * Removes <b>all</b> bindings.
	 */
	public void clear() {
		// Remove bindings
		for(var list : bindings.values()) {
			list.clear();
		}

		// Unbind devices
		for(var device : inverse.keySet()) {
			device.remove();
		}
		inverse.clear();
	}

	@Override
	public int hashCode() {
		return bindings.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof ActionBindings that) &&
				this.bindings.equals(that.bindings);
	}

	@Override
	public String toString() {
		return bindings.toString();
	}
}
