package org.sarge.jove.platform.desktop;

import java.util.function.*;

import org.sarge.jove.control.Event;
import org.sarge.jove.control.Event.*;

import com.sun.jna.Callback;

/**
 * Base-class for a device attached to a GLFW window.
 * @author Sarge
 */
abstract class DesktopDevice implements Device {
	private final Window window;

	/**
	 * Constructor.
	 * @param window Window
	 */
	protected DesktopDevice(Window window) {
		this.window = window;
	}

	/**
	 * Template implementation for a source based on a GLFW callback.
	 * @param <T> GLFW callback
	 * @param <E> Event type
	 */
	abstract class DesktopSource<T extends Callback, E extends Event> implements Source<E> {
		/**
		 * Creates a listener that generates events and delegates to the given handler.
		 * @param handler Event handler
		 * @return New GLFW listener
		 */
		protected abstract T listener(Consumer<E> handler);

		/**
		 * Provides the registration method for the listener.
		 * @param lib GLFW library
		 * @return Listener registration method
		 */
		protected abstract BiConsumer<Window, T> method(DesktopLibrary lib);

		@Override
		public final void bind(Consumer<E> handler) {
			// Retrieve listener registration method
			final DesktopLibrary lib = window.desktop().library();
			final BiConsumer<Window, T> method = method(lib);

			// Register listener
			if(handler == null) {
				method.accept(window, null);
			}
			else {
				final T listener = listener(handler);
				method.accept(window, listener);
				window.register(handler, listener);
			}
		}
	}
}
