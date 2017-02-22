package org.sarge.jove.animation;

import org.sarge.jove.app.FrameListener;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Animator for an {@link Animation}.
 * @author Sarge
 */
public class Animator extends Player implements FrameListener {
	/**
	 * Defines something that can be animated.
	 */
	@FunctionalInterface
	public interface Animation {
		/**
		 * Updates this animation.
		 * @param time			Current animation time (ms)
		 * @param duration		Animation duration (ms)
		 */
		void update(long time, long duration);
	}
	
	private final Animation animation;
	private final long duration;

	private long time;

	/**
	 * Constructor.
	 * @param animation		Animation managed by this animator
	 * @param duration		Animation duration (ms)
	 */
	public Animator(Animation animation, long duration) {
		Check.notNull(animation);
		Check.oneOrMore(duration);
		this.animation = animation;
		this.duration = duration;
		setRepeating(true);
	}

	/**
	 * @return Current animation time
	 */
	public long getTime() {
		return time;
	}

	@Override
	public void update(long t, long elapsed) {
		// Ignore if not running
		if(!isPlaying()) return;

		// Update animation time
		time += elapsed * super.getSpeed();

		// Check whether finished
		final boolean finished = !isRepeating() && (time > duration);
		
		if(finished) {
			// Stop at end of non-repeating animation
			time = duration;
			stop();
		}
		else {
			// Quantize to animation duration
			time = time % duration;
		}

		// Update animation
		animation.update(time, duration);
	}

	@Override
	public String toString() {
		return ToString.toString(this);
	}
}
