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
	private Consumer<E> listener;

	// TODO - this fails if multiple instances created => enforce max one per window? i.e. private ctors for devices?
	// or revert to registry on window (messy?)
	// i.e. 'listener' above doesn't help globally if created (say) multiple KeyboardDevice(window)
	// only used to ensure bind/remove pairs

	/**
	 * Constructor.
	 * @param window Parent window
	 */
	protected AbstractWindowDevice(Window window) {
		this.window = requireNonNull(window);
	}

	/**
	 * @return Listener bound to this device
	 */
	protected Consumer<E> listener() {
		return listener;
	}

	@Override
	public T bind(Consumer<E> listener) {
		requireNonNull(listener);
		if(Objects.nonNull(this.listener)) {
			throw new IllegalStateException("Device already bound: " + this);
		}

		final T callback = callback(window, listener);
		final BiConsumer<Window, T> method = method((DeviceLibrary) window.library());
		method.accept(window, callback);
		this.listener = listener;

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
		if(this.listener == null) {
			throw new IllegalStateException("Device is not bound");
		}

		final BiConsumer<Window, T> method = method((DeviceLibrary) window.library());
		method.accept(window, null);

		this.listener = null;
	}

	@Override
	public int hashCode() {
		return Objects.hash(window, listener);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof AbstractWindowDevice that) &&
				this.window.equals(that.window) &&
				this.listener.equals(that.listener);
	}

	@Override
	public String toString() {
		return String.format("AbstractWindowDevice[window=%s listener=%s]", window, listener);
	}
}
