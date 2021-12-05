package org.sarge.jove.platform.desktop;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.sarge.jove.control.Button.Action;
import org.sarge.jove.control.Event;
import org.sarge.jove.control.Event.Device;
import org.sarge.jove.control.Event.Source;

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
	 * Maps a GLFW action code.
	 * @param action Action code
	 * @return Action
	 */
	static Action map(int action) {
		return switch(action) {
			case 0 -> Action.RELEASE;
			case 1 -> Action.PRESS;
			case 2 -> Action.REPEAT;
			default -> throw new RuntimeException("Unsupported action code: " + action);
		};
	}

	/**
	 * Template implementation for a source based on a GLFW callback.
	 * @param <T> GLFW callback
	 */
	abstract class DesktopSource<T, E extends Event> implements Source<E> {
		@SuppressWarnings("unused")
		private T listener;

		/**
		 * Creates a listener that generates events and delegates to the given handler.
		 * @param handler Event handler
		 * @return New GLFW listener
		 */
		protected abstract T listener(Consumer<Event> handler);

		/**
		 * Provides the registration method for the listener.
		 * @param lib GLFW library
		 * @return Listener registration method
		 */
		protected abstract BiConsumer<Window, T> method(DesktopLibrary lib);

		@Override
		public final void bind(Consumer<Event> handler) {
			if(handler == null) {
				bind((T) null);
			}
			else {
				bind(listener(handler));
			}
		}

		private void bind(T listener) {
			final DesktopLibrary lib = window.desktop().library();
			final BiConsumer<Window, T> method = method(lib);
			method.accept(window, listener);
			this.listener = listener;
		}
	}
}
