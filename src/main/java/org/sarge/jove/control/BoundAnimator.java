package org.sarge.jove.control;

import static org.sarge.lib.util.Check.*;

import java.time.Duration;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.util.Interpolator;

/**
 * A <i>bound animator</i> interpolates the animation position over a given duration.
 * @author Sarge
 */
public class BoundAnimator extends Animator {
	private final long duration;
	private final Interpolator interpolator;
	private long time;

	/**
	 * Constructor.
	 * @param animation			Animation
	 * @param duration 			Duration
	 * @param interpolator		Elapsed time interpolator
	 */
	public BoundAnimator(Animation animation, Duration duration, Interpolator interpolator) {
		super(animation);
		this.duration = oneOrMore(duration.toMillis());
		this.interpolator = notNull(interpolator);
	}

	/**
	 * Constructor for a non-interpolated animation.
	 * @param animation			Animation
	 * @param duration 			Duration
	 */
	public BoundAnimator(Animation animation, Duration duration) {
		this(animation, duration, Interpolator.IDENTITY);
	}

	/**
	 * @return Duration of this animator
	 */
	public Duration duration() {
		return Duration.ofMillis(duration);
	}

	@Override
	public float elapsed() {
		return interpolator.interpolate(time / (float) duration);
	}

	@Override
	public void update() {
		// Update animation position
		time += super.elapsed();

		// Check for end of animation
		if(time > duration) {
			if(isRepeating()) {
				// Cycle animation
				time = time % duration;
			}
			else {
				// Interrupt animation
				time = duration;
				animation.update(this);
				stop();
				return;
			}
		}

		// Delegate
		super.update();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append(String.format("%d/%d", time, duration))
				.build();
	}
}
