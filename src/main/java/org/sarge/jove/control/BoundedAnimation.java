package org.sarge.jove.control;

import java.time.Duration;

import org.sarge.jove.control.Animator.Animation;

/**
 * A <i>bounded animation</i> is an adapter that interpolates animation updates over a given duration.
 * @author Sarge
 */
public abstract class BoundedAnimation implements Animation {
	private final long duration;
	private long time;
	private boolean repeat = true;

	/**
	 * Constructor.
	 * @param duration Animation duration
	 */
	public BoundedAnimation(Duration duration) {
		this.duration = duration.toMillis();
	}

	/**
	 * Sets whether this animation repeats (default is {@code true}).
	 * @param repeat Whether repeating
	 */
	public void repeat(boolean repeat) {
		this.repeat = repeat;
	}

	/**
	 * Updates this bounded animation.
	 * @param pos Animation position 0..1
	 */
	protected abstract void update(float pos);

	@Override
	public boolean update(Animator animator) {
		// Update animation time
		time += animator.elapsed();

		// Check whether finished or cycle
		if(time > duration) {
			if(repeat) {
				time = time % duration;
			}
			else {
				update(1);
				return true;
			}
		}

		// Update animation position
		update(time / (float) duration);
		return false;
	}
}
