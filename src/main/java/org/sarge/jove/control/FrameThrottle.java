package org.sarge.jove.control;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

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
		this.duration = TimeUnit.SECONDS.toNanos(1) / fps;
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
	 * @param duration Sleep duration (nanoseconds)
	 */
	@SuppressWarnings("static-method")
	protected void sleep(long duration) {
		LockSupport.parkNanos(duration);
//		try {
//			Thread.sleep(ms, nano);
//		}
//		catch(InterruptedException e) {
//			interrupted(e);
//		}
	}

//	/**
//	 * Notifies that the thread was interrupted while sleeping.
//	 * @param e Interrupted exception
//	 */
//	protected void interrupted(InterruptedException e) {
//		// Ignored
//	}
}
