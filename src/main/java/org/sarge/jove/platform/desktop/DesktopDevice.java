package org.sarge.jove.platform.desktop;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.sarge.jove.control.Button.Action;
import org.sarge.jove.control.Event;
import org.sarge.jove.control.Event.Device;
import org.sarge.jove.control.Event.Source;

/**
 * Base-class for a GLFW-based device.
 * @author Sarge
 */
public abstract class DesktopDevice implements Device {
	private final Window window;

	/**
	 * Constructor.
	 * @param window Window
	 */
	protected DesktopDevice(Window window) {
		this.window = window;
	}

	@Override
	public void close() {
		// Does nowt
	}

	/**
	 * Maps a GLFW action code.
	 * @param action Action code
	 * @return Action
	 */
	protected static Action map(int action) {
		return switch(action) {
			case 0 -> Action.RELEASE;
			case 1 -> Action.PRESS;
			case 2 -> Action.REPEAT;
			default -> throw new RuntimeException("Unsupported action code: " + action);
		};
	}

	/**
	 * Template implementation for a source based on a GLFW callback.
	 * @param <T> Callback
	 */
	public abstract class DesktopSource<T> implements Source {
		@SuppressWarnings("unused")
		private T listener;

		/**
		 * Creates a listener that generates events and delegates to the given handler.
		 * @param handler Event handler
		 * @return New GLFW listener
		 */
		protected abstract T listener(Consumer<Event> handler);

		/**
		 * Returns the registration method for the listener.
		 * @param lib GLFW library
		 * @return Listener registration method
		 */
		protected abstract BiConsumer<Window, T> method(DesktopLibrary lib);

		@Override
		public final Device device() {
			return DesktopDevice.this;
		}

		/**
		 * Registers the listener with the service and the window.
		 * @param listener Listener
		 */
		private void register(T listener) {
			final DesktopLibrary lib = window.desktop().library();
			final BiConsumer<Window, T> method = method(lib);
			method.accept(window, listener);
			this.listener = listener;
		}

		@Override
		public final void bind(Consumer<Event> handler) {
			final T listener = listener(handler);
			register(listener);
		}

		@Override
		public final void disable() {
			register(null);
		}
	}
}
