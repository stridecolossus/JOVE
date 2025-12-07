package org.sarge.jove.platform.desktop;

import static java.util.Objects.requireNonNull;

import java.util.function.*;

import org.sarge.jove.control.Event;
import org.sarge.jove.foreign.Callback;

/**
 * Skeleton implementation for a GLFW device with a parent window.
 * @param <T> Callback type
 * @param <E> Event type
 * @author Sarge
 */
abstract class AbstractWindowDevice<E extends Event, T extends Callback> implements Device<E> {
	private final Window window;

	/**
	 * Constructor.
	 * @param window Parent window
	 */
	protected AbstractWindowDevice(Window window) {
		this.window = requireNonNull(window);
	}

	@Override
	public void bind(Consumer<E> listener) {
		final T callback = callback(window, listener);
		final BiConsumer<Window, T> method = method((DeviceLibrary) window.library());
		method.accept(window, callback);
		window.register(listener, callback);
	}

	/**
	 * Creates the callback for the given window and event listener.
	 * @param window		Window
	 * @param listener		Event listener
	 * @return Callback
	 */
	protected abstract T callback(Window window, Consumer<E> listener);

	/**
	 * Provides the GLFW setter method for this device.
	 * @param library Device library
	 * @return Callback setter
	 */
	protected abstract BiConsumer<Window, T> method(DeviceLibrary library);

	@Override
	public void remove(Consumer<E> listener) {
		window.remove(listener);
	}
}
