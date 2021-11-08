package org.sarge.jove.control;

import static org.sarge.lib.util.Check.notNull;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * The <i>frame tracker</i> is a render loop task that maintains per-frame time information.
 * @author Sarge
 */
public class FrameTracker implements RenderLoop.Task {
	/**
	 * Listener for frame events.
	 */
	public interface Listener {
		/**
		 * Notifies a new frame.
		 * @param tracker Frame tracker
		 */
		void update(FrameTracker tracker);
	}

	/**
	 * @return Current time (nanoseconds)
	 */
	private static long now() {
		return System.nanoTime();
	}

	private final Set<Listener> listeners = new HashSet<>();

	private long time = now();
	private long elapsed;

	/**
	 * @return Current time (nanoseconds)
	 */
	public long time() {
		return time;
	}

	/**
	 * @return Elapsed time since previous frame (nanoseconds)
	 */
	public long elapsed() {
		return elapsed;
	}

	/**
	 * Adds a frame listener.
	 * @param listener Listener to add
	 */
	public void add(Listener listener) {
		listeners.add(notNull(listener));
	}

	/**
	 * Removes a frame listener.
	 * @param listener Listener to remove
	 */
	public void remove(Listener listener) {
		listeners.remove(listener);
	}

	/**
	 * Removes all frame listeners.
	 */
	public void clear() {
		listeners.clear();
	}

	@Override
	public void execute() {
		// Update times
		final long now = now();
		elapsed = now - time;
		time = now;

		// Notify listeners
		for(Listener listener : listeners) {
			listener.update(this);
		}
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("time", time)
				.append("elapsed", elapsed)
				.build();
	}
}
