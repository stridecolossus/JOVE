package org.sarge.jove.scene;

import static org.sarge.lib.util.Check.notNull;

import java.util.*;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * A <i>frame counter</i> is an adapter for a render task that also tracks frame completion events and FPS.
 * @author Sarge
 */
public class FrameCounter implements Runnable {
	/**
	 * Listener for completed frames.
	 */
	public interface Listener {
		/**
		 * Notifies a completed frame.
		 * @param time			Completion time
		 * @param elapsed		Elapsed time (ms)
		 */
		void frame(long time, long elapsed);
	}

	private static final long SECOND = TimeUnit.SECONDS.toMillis(1);

	private final Runnable task;
	private final Set<Listener> listeners = new HashSet<>();

	private long end;
	private int count;
	private int fps;

	/**
	 * Constructor.
	 * @param task Render task
	 */
	public FrameCounter(Runnable task) {
		this.task = notNull(task);
	}

	/**
	 * @return Number of rendered frames during the current interval
	 */
	public int count() {
		return count;
	}

	/**
	 * @return Frames-per-second
	 */
	public int fps() {
		return fps;
	}

	/**
	 * Registers a frame listener.
	 * @param listener Listener to add
	 */
	public void add(Listener listener) {
		listeners.add(notNull(listener));
	}

	@Override
	public void run() {
		// Record start time
		final long start = System.currentTimeMillis();

		// Delegate
		task.run();

		// Update FPS
		if(start > end) {
			fps = count;
			count = 1;
			end = start + SECOND;
		}
		else {
			++count;
		}

		// Notify listeners
		final long now = System.currentTimeMillis();
		final long elapsed = now - start;
		for(Listener listener : listeners) {
			listener.frame(now, elapsed);
		}
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("count", count)
				.append("FPS", fps)
				.build();
	}
}
