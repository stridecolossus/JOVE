package org.sarge.jove.platform.desktop;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.function.*;

import org.sarge.jove.control.*;
import org.sarge.jove.foreign.Callback;

/**
 * Skeleton implementation for a GLFW device with a parent window.
 * @param <T> Callback type
 * @param <E> Event type
 * @author Sarge
 */
abstract class AbstractWindowDevice<E extends Event, T extends Callback> implements Device<E> {
	private final Window window;
	private T callback;

	/**
	 * Constructor.
	 * @param window Parent window
	 */
	protected AbstractWindowDevice(Window window) {
		this.window = requireNonNull(window);
	}

	@Override
	public boolean isBound() {
		return Objects.nonNull(callback);
	}

	@Override
	public T bind(Consumer<E> listener) {
		requireNonNull(listener);
		if(isBound()) {
			throw new IllegalStateException("Device already bound: " + this);
		}

		// Create callback instance
		callback = callback(window, listener);

		// Bind listener to callback
		final BiConsumer<Window, T> method = method((DeviceLibrary) window.library());
		method.accept(window, callback);

		return callback;
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
	public void remove() {
		if(!isBound()) {
			throw new IllegalStateException("Device is not bound");
		}

		final BiConsumer<Window, T> method = method((DeviceLibrary) window.library());
		method.accept(window, null);

		callback = null;
	}

	@Override
	public int hashCode() {
		return Objects.hash(window, callback);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof AbstractWindowDevice that) &&
				this.window.equals(that.window) &&
				this.callback.equals(that.callback);
	}

	@Override
	public String toString() {
		return String.format("AbstractWindowDevice[window=%s callback=%s]", window, callback);
	}
}
