package org.sarge.jove.common;

import java.util.Collection;

import org.sarge.lib.collection.StrictSet;
import org.sarge.lib.util.AbstractObject;

/**
 * A <i>frame</i> is a model for frame statistics.
 * @author Sarge
 */
public class Frame extends AbstractObject {
	/**
	 * Listener for frame updates.
	 */
	@FunctionalInterface
	public interface Listener {
		/**
		 * Notifies the start of the next frame.
		 * @param frame Frame
		 */
		void update(Frame frame);
	}

	private long now = System.currentTimeMillis();
	private long end;
	private long elapsed;
	private int rate;
	private int count;

	private final Collection<Listener> listeners = new StrictSet<>();

	/**
	 * Constructor.
	 */
	public Frame() {
		init();
	}

	/**
	 * Initialises the expiry time of the next frame.
	 */
	private void init() {
		end = now + 1000;
	}

	/**
	 * @return Current time
	 */
	public long now() {
		return now;
	}

	/**
	 * @return Elapsed time since previous frame (ms)
	 */
	public long elapsed() {
		return elapsed;
	}

	/**
	 * @return Current frame-rate
	 */
	public int rate() {
		return rate;
	}

	/**
	 * Starts the next frame.
	 */
	public void update() {
		// Calculate elapsed frame duration
		final long next = System.currentTimeMillis();
		elapsed = next - now;
		now = next;

		// Count frames
		++count;

		// Update frame-rate every second
		if(now >= end) {
			rate = count;
			count = 0;
			init();
			// TODO - expiry ~ difference from end of previous frame?
		}

		// Notify listeners
		listeners.forEach(listener -> listener.update(this));
	}

	/**
	 * Registers a frame-listener.
	 * @param listener Listener
	 */
	public void add(Listener listener) {
		listeners.add(listener);
	}
}
