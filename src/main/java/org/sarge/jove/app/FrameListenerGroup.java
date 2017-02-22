package org.sarge.jove.app;

import java.util.List;

import org.sarge.lib.util.StrictList;
import org.sarge.lib.util.ToString;

/**
 * Group of frame listeners.
 * @author Sarge
 */
public class FrameListenerGroup implements FrameListener {
	private final List<FrameListener> listeners = new StrictList<>();

	/**
	 * Adds a listener to this group.
	 * @param listener Listener to add
	 */
	public void add(FrameListener listener) {
		listeners.add(listener);
	}

	/**
	 * Removes a listener from this group.
	 * @param listener Listener to remove
	 */
	public void remove(FrameListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Removes all listeners.
	 */
	public void clear() {
		listeners.clear();
	}

	@Override
	public void update(long time, long elapsed) {
		for(FrameListener listener : listeners) {
			listener.update(time, elapsed);
		}
	}

	@Override
	public String toString() {
		return ToString.toString(this);
	}
}
