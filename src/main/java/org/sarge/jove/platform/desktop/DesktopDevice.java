package org.sarge.jove.platform.desktop;

import static java.util.stream.Collectors.joining;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.control.Event;
import org.sarge.jove.control.Event.Device;
import org.sarge.jove.control.Event.Source;

/**
 * Convenience base-class for a GLFW-based device.
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
	 * GLFW action codes.
	 */
	protected enum Action {
		RELEASE,
		PRESS,
		REPEAT;

		private static final Action[] ACTIONS = Action.values();

		/**
		 * Maps an action code to an event token.
		 * @param action Action code
		 * @return Action token
		 */
		public static String map(int action) {
			return ACTIONS[action].name();
		}
	}

	/**
	 * GLFW event modifiers.
	 */
	protected enum Modifier implements IntegerEnumeration {
		SHIFT(0x0001),
		CONTROL(0x0002),
		ALT(0x0004),
		SUPER(0x0008),
		CAPS_LOCK(0x0010),
		NUM_LOCK(0x0020);

		private final int value;

		private Modifier(int value) {
			this.value = value;
		}

		@Override
		public int value() {
			return value;
		}

		/**
		 * Maps a GLFW modifier mask to event name tokens.
		 * @param mods Modifiers mask
		 * @return Modifier token(s)
		 * @see #name(String...)
		 */
		protected static String map(int mods) {
			return IntegerEnumeration
					.enumerate(Modifier.class, mods)
					.stream()
					.map(Enum::name)
					.collect(joining("-"));
		}
	}

	/**
	 * Helper - Builds a GLFW event name from the given components.
	 * @param name			Event identifier
	 * @param action		Action code
	 * @param mods			Modifier mask
	 * @return Event name
	 */
	protected static String name(String name, int action, int mods) {
		return Event.name(name, Action.map(action), Modifier.map(mods));
	}

	/**
	 * Template implementation for a source based on a GLFW callback.
	 * @param <T> Callback
	 */
	public abstract class DesktopSource<T> implements Source {
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

		/**
		 * Registers the listener with the service and the window.
		 * @param listener GLFW listener
		 */
		private void register(T listener) {
			final DesktopLibrary lib = window.desktop().library();
			final BiConsumer<Window, T> method = method(lib);
			method.accept(window, listener);
		}

		@Override
		public final Device device() {
			return DesktopDevice.this;
		}

		@Override
		public final void bind(Consumer<Event> handler) {
			final T listener = listener(handler);
			register(listener);
			window.register(handler, listener);
		}

		@Override
		public final void disable() {
			register(null);
		}
	}
}
