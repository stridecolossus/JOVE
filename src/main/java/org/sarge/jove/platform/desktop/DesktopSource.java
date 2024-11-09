package org.sarge.jove.platform.desktop;

import java.util.function.*;

import javax.security.auth.callback.Callback;

import org.sarge.jove.control.Event;
import org.sarge.jove.control.Event.Source;

/**
 * A <i>desktop source</i> is a template for an event source based on a GLFW callback.
 * <p>
 * The over-ridden {@link #bind(Consumer)} method encapsulates the following steps:
 * <ol>
 * <li>Create a new GLFW listener using {@link #listener(Consumer)}</li>
 * <li>Invoke {@link #method(DesktopLibrary)} to register the listener with GLFW</li>
 * <li>Also register the listener with the parent window via {@link Window#register(Object, Callback)} to prevent listeners being garbage-collected</li>
 * </ol>
 * <p>
 * @param <T> GLFW callback
 * @param <E> Event type
 * @author Sarge
 */
interface DesktopSource<T /*extends Callback*/, E extends Event> extends Source<E> {
	/**
	 * @return Parent window
	 */
	Window window();

	/**
	 * Creates a listener that generates events and delegates to the given handler.
	 * @param handler Event handler
	 * @return New GLFW listener
	 */
	T listener(Consumer<E> handler);

	/**
	 * Provides the registration method for the listener.
	 * @param lib GLFW library
	 * @return Listener registration method
	 */
	BiConsumer<Window, T> method(DesktopLibrary lib);

	@Override
	default T bind(Consumer<E> handler) {
		// Retrieve listener registration method
		final Window window = this.window();
		final DesktopLibrary lib = window.desktop().library();
		final BiConsumer<Window, T> method = method(lib);

		// Register listener
		if(handler == null) {
			method.accept(window, null);
//			window.remove(handler);
			return null;
		}
		else {
			final T listener = listener(handler);
			method.accept(window, listener);
//			window.register(handler, listener);
			return listener;
		}
	}
}
