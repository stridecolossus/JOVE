package org.sarge.jove.control;

import static org.sarge.lib.util.Check.*;

import java.time.*;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * An <i>animator</i> is a specialised playable for an {@link Animation} that is updated per frame.
 * @author Sarge
 */
public class Animator extends Playable implements FrameListener {
	/**
	 * An <i>animation</i> is updated by this animator.
	 */
	@FunctionalInterface
	public interface Animation {
		/**
		 * Updates this animation.
		 * @param animator Animator
		 */
		void update(Animator animator);
	}

	private final long duration;
	private final Animation animation;

	private float speed = 1;
	private long time;
	private float pos;

	/**
	 * Constructor.
	 * @param duration		Duration (ms)
	 * @param animation		Animation
	 */
	public Animator(long duration, Animation animation) {
		this.duration = oneOrMore(duration);
		this.animation = notNull(animation);
	}

	/**
	 * @return Duration of this animation (ms)
	 */
	public long duration() {
		return duration;
	}

	/**
	 * @return Current time position within the animation duration (ms)
	 */
	public long time() {
		return time;
	}

	/**
	 * @return Animation <i>position</i> as a floating-point value in the range 0..1
	 */
	public float position() {
		return pos;
	}

	/**
	 * @return Animation speed
	 */
	public float speed() {
		return speed;
	}

	/**
	 * Sets the animation speed.
	 * @param speed Speed
	 * @throws IllegalArgumentException if the speed is not positive
	 */
	public void speed(float speed) {
		if(speed <= 0) throw new IllegalArgumentException("Speed must be positive");
		this.speed = speed;
	}

	@Override
	public void frame(Instant start, Instant end) {
		// Ignore if stopped or paused
		if(!isPlaying()) {
			return;
		}

		// Update time position
		final long elapsed = Duration.between(start, end).toMillis();
		time += speed * elapsed;

		// Check for completed animation
		if(time > duration) {
			if(isRepeating()) {
				time = time % duration;
			}
			else {
				time = duration;
				state(Playable.State.STOP);
			}
		}

		// Update animation
		pos = time / (float) duration;
		animation.update(this);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append(String.format("%d/%d", time, duration))
				.append(animation)
				.build();
	}
}
