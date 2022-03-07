package org.sarge.jove.control;

import java.util.concurrent.TimeUnit;

/**
 * The <i>frame throttle</i> sleeps the rendering thread for a specified duration between each frame.
 * @author Sarge
 */
public class FrameThrottle implements FrameTracker.Listener {
	private long duration;

	/**
	 * Constructor.
	 */
	public FrameThrottle() {
		throttle(50);
	}

	/**
	 * Sets the target throttle rate.
	 * @param fps Target frames-per-second (default is 50)
	 */
	public void throttle(int fps) {
		this.duration = TimeUnit.SECONDS.toMillis(1) / fps;
	}

	@Override
	public void update(FrameTracker tracker) {
		final long sleep = duration - tracker.elapsed();
		if(sleep > 0) {
			try {
				Thread.sleep(sleep);
			}
			catch(InterruptedException e) {
				// Ignored
			}
		}
	}
}
