package org.sarge.jove.control;

import static org.sarge.lib.util.Check.zeroOrMore;

/**
 * The <i>frame throttle</i> sleeps the rendering thread for a specified duration between each frame.
 * @author Sarge
 */
public class FrameThrottle implements FrameTracker.Listener {
	private long duration = 50;

	/**
	 * Sets the throttle duration.
	 * @param duration Throttle duration (default is 50ms)
	 */
	public void duration(long duration) {
		this.duration = zeroOrMore(duration);
	}

	@Override
	public void update(FrameTracker tracker) {
		final long sleep = duration - tracker.elapsed();
		if(sleep > 0) {
			sleep(sleep);
		}
	}

	/**
	 * Sleeps the current thread for the given duration.
	 * @param duration Sleep duration (ms)
	 */
	protected void sleep(long duration) {
		try {
			Thread.sleep(duration);
		}
		catch(InterruptedException e) {
			interrupted(e);
		}
	}

	/**
	 * Notifies that the thread was interrupted while sleeping.
	 * @param e Interrupted exception
	 */
	protected void interrupted(InterruptedException e) {
		// Ignored
	}
}
