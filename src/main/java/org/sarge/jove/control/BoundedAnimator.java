package org.sarge.jove.control;

import java.time.Duration;

/**
 * A <i>bounded animator</i> interpolates an animation over a given duration.
 * @author Sarge
 */
public class BoundedAnimator extends Animator {
	private final long bound;
	private boolean repeat = true;
	private float pos;
	private boolean stopped;

	/**
	 * Constructor.
	 * @param animation 	Delegate animation
	 * @param duration		Bounding duration
	 */
	public BoundedAnimator(Animation animation, Duration duration) {
		super(animation);
		this.bound = duration.toMillis();
	}

	@Override
	public boolean isPlaying() {
		if(stopped) {
			return false;
		}
		else {
			return super.isPlaying();
		}
	}

	@Override
	public float position() {
		return pos;
	}

	/**
	 * @return Whether this animator is repeating
	 */
	public boolean isRepeating() {
		return repeat;
	}

	/**
	 * Sets whether this animator repeats.
	 * @param repeat Whether repeats
	 */
	public void repeat(boolean repeat) {
		this.repeat = repeat;
	}

	@Override
	protected void update() {
		stopped = false;

		final long time = time();
		if(time > bound) {
			if(repeat) {
				pos = time % bound;
			}
			else {
				pos = bound;
				stopped = true;
			}
		}
		else {
			pos = time;
		}

		super.update();
	}
}
