package org.sarge.jove.platform;

import static org.sarge.lib.util.Check.notNull;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.control.Event;
import org.sarge.jove.control.Event.Handler;

/**
 * A <i>device</i> manages platform-specific input event callbacks bound to client event handlers.
 * @author Sarge
 * @param <T> Event source type
 */
public class Device<T> {
	/**
	 * Device entry.
	 * @param <T> Event source type
	 * @param <C> Callback type
	 */
	public static final class Entry<T, C> {
		private final Function<Event.Handler, C> factory;
		private final BiConsumer<T, C> binder;

		/**
		 * Constructor.
		 * @param factory		Creates an event listener
		 * @param binder		Binds an event listener to the window
		 */
		public Entry(Function<Handler, C> factory, BiConsumer<T, C> binder) {
			this.factory = notNull(factory);
			this.binder = notNull(binder);
		}

		/**
		 * Binds the given event handler to this device callback.
		 * @param src			Event source
		 * @param handler		Event handler
		 */
		private void bind(T src, Event.Handler handler) {
			final C listener = factory.apply(handler);
			attach(src, listener);
		}

		/**
		 * Attaches an event listener.
		 * @param src			Event source
		 * @param listener		Listener to attach or <tt>null</tt> to remove
		 */
		private void attach(T src, C listener) {
			binder.accept(src, listener);
		}
	}

	private final Map<Event.Category, Entry<T, ?>> entries;
	private final T src;
	private final Set<Event.Category> active = new HashSet<>();

	/**
	 * Constructor.
	 * @param src			Event source
	 * @param entries		Device entries ordered by category
	 */
	public Device(T src, Map<Event.Category, Entry<T, ?>> entries) {
		this.src = notNull(src);
		this.entries = Map.copyOf(entries);
	}

	/**
	 * @return Event categories supported by this device
	 */
	public Set<Event.Category> categories() {
		return entries.keySet();
	}

	/**
	 * @return Active event categories
	 */
	public Set<Event.Category> active() {
		return new HashSet<>(active);
	}

	/**
	 * Binds an event category to the given handler.
	 * @param cat			Event category
	 * @param handler		Handler
	 * @throws UnsupportedOperationException if this device does not support the given category
	 */
	public void bind(Event.Category cat, Event.Handler handler) {
		final Entry<T, ?> entry = entries.get(cat);
		if(entry == null) throw new UnsupportedOperationException("Event category not supported by this device: " + cat);
		entry.bind(src, handler);
		active.add(cat);
	}

	/**
	 * Removes the event handler bound to the given category.
	 * @param cat Event category
	 * @throws IllegalStateException if a handler has not been bound for the given category
	 */
	public void remove(Event.Category cat) {
		if(!active.contains(cat)) throw new IllegalStateException("No active handler: " + cat);
		final Entry<T, ?> entry = entries.get(cat);
		entry.attach(src, null);
		active.remove(cat);
	}

	/**
	 * Removes all active handlers.
	 */
	public void clear() {
		for(Event.Category cat : active) {
			final Entry<T, ?> entry = entries.get(cat);
			entry.attach(src, null);
		}
		active.clear();
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
