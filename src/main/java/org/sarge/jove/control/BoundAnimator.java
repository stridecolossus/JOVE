package org.sarge.jove.control;

import static org.sarge.lib.util.Check.oneOrMore;

/**
 * A <i>bound animator</i> interpolates the position of an animation over a given duration.
 * <p>
 * If this animator {@link #isRepeating()} the animation is cycled, otherwise the animation stops at the end of the given duration.
 * <p>
 * @author Sarge
 */
public class BoundAnimator extends Animator {
	private final long duration;
	private long time;

	/**
	 * Constructor.
	 * @param duration		Duration (ms)
	 * @param animation 	Animation
	 */
	public BoundAnimator(long duration, Animation animation) {
		super(animation);
		this.duration = oneOrMore(duration);
	}

	@Override
	public float position() {
		return time / (float) duration;
	}

	@Override
	protected void update(long elapsed) {
		// Update time position
		time += elapsed;

		// Check for completed animation
		if(time > duration) {
			if(isRepeating()) {
				time = time % duration;
			}
			else {
				time = duration;
				state(State.STOP);
			}
		}
	}
}
