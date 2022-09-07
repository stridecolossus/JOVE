package org.sarge.jove.control;

import static org.sarge.lib.util.Check.oneOrMore;

import java.time.Duration;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * A <i>bound animator</i> interpolates the animation position over a given duration.
 * @author Sarge
 */
public class BoundAnimator extends Animator {
	private final long duration;

	private long time;

	/**
	 * Constructor.
	 * @param animation		Animation
	 * @param duration 		Duration
	 */
	public BoundAnimator(Animation animation, Duration duration) {
		super(animation);
		this.duration = oneOrMore(duration.toMillis());
	}

	/**
	 * @return Duration of this animator
	 */
	public Duration duration() {
		return Duration.ofMillis(duration);
	}

	@Override
	public float elapsed() {
		return time / (float) duration;
	}

	@Override
	public void update() {
		// Update animation position
		time += super.elapsed();

		// Cycle or stop animation
		if(time > duration) {
			if(isRepeating()) {
				time = time % duration;
			}
			else {
				stop();
				time = duration;
			}
		}

		// Update animation
		animate();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append(String.format("%d/%d", time, duration))
				.build();
	}
}
