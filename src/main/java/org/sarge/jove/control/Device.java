package org.sarge.jove.control;

import java.util.function.Consumer;

import org.sarge.jove.foreign.Callback;

/**
 * A <i>device</i> is a hardware component that generates input events, such as the keyboard, mouse, or a controller.
 * @param <E> Event type
 * @author Sarge
 */
public interface Device<E extends Event> {
	/**
	 * @return Whether this device is currently bound to an event handler
	 */
	boolean isBound();

	/**
	 * Binds an event handler to this device.
	 * @param handler Event handler
	 * @return Underlying callback
	 * @throws IllegalStateException if this device is already bound
	 */
	Callback bind(Consumer<E> handler);

	/**
	 * Removes the attached event handler.
	 * @throws IllegalArgumentException if this device is not bound
	 */
	void remove();
}
