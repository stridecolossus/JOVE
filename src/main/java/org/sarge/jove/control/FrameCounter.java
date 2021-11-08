package org.sarge.jove.control;

import java.util.concurrent.TimeUnit;

/**
 * The <i>frame counter</i> tracks the number of frames-per-second.
 * @author Sarge
 */
public class FrameCounter implements FrameTracker.Listener {
	private static final long SECOND = TimeUnit.SECONDS.toNanos(1);

	private long time;
	private int count;
	private int current;

	/**
	 * @return Number of frames in the <i>previous</i> period
	 */
	public int count() {
		return count;
	}

	@Override
	public void update(FrameTracker tracker) {
		// Increment
		time += tracker.elapsed();
		++current;

		// Reset after each second
		if(time >= SECOND) {
			reset();
		}
	}

	/**
	 * Resets this counter after each elapsed second.
	 */
	protected void reset() {
		count = current;
		current = 0;
		time = time % SECOND;
	}
}
