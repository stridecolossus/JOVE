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
	 * Binds an event listener to this device.
	 * @param listener Event listener
	 * @return Underlying callback
	 * @throws IllegalStateException if this device is already bound to a listener
	 */
	Callback bind(Consumer<E> listener);

	/**
	 * Removes the attached event listener.
	 * @throws IllegalArgumentException if a listener is not bound to this device
	 */
	void remove();
}
