package org.sarge.jove.platform.desktop;

import java.util.function.Consumer;

import org.sarge.jove.control.Event;

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
	Object bind(Consumer<E> listener);

	/**
	 * Removes the attached event listener.
	 * @param listener Event listener to remove
	 * @throws IllegalArgumentException if the listener is not bound to this device
	 */
	void remove(Consumer<E> listener);
}
