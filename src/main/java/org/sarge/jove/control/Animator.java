package org.sarge.jove.control;

import static org.sarge.lib.util.Check.*;

import java.time.Duration;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * An <i>animator</i> is a specialised playable for an {@link Animation} updated on each frame.
 * @author Sarge
 */
public class Animator extends AbstractPlayable implements Frame.Listener {
	/**
	 * An <i>animation</i> is updated by this animator.
	 */
	@FunctionalInterface
	public interface Animation {
		/**
		 * Updates this animation.
		 * @param pos Animation position 0..1
		 */
		void update(float pos);
	}

	private final Animation animation;
	private final long duration;
	private long time;
	private float speed = 1;
	private boolean repeat = true;

	/**
	 * Constructor.
	 * @param animation 	Animation
	 * @param duration		Duration
	 */
	public Animator(Animation animation, Duration duration) {
		this.animation = notNull(animation);
		this.duration = oneOrMore(duration.toMillis());
	}

	/**
	 * @return Animation controlled by this animator
	 */
	public Animation animation() {
		return animation;
	}

	/**
	 * @return Animation duration
	 */
	public Duration duration() {
		return Duration.ofMillis(duration);
	}

	/**
	 * @return Current animation position
	 */
	public Duration time() {
		return Duration.ofMillis(time);
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

	/**
	 * @return Whether this animator is repeating
	 */
	public boolean isRepeating() {
		return repeat;
	}

	/**
	 * Sets whether to repeat the animation.
	 * @param repeat Whether repeating
	 */
	public void repeat(boolean repeat) {
		this.repeat = repeat;
	}

	@Override
	public void update(Frame frame) {
		// Ignore if stopped or paused
		if(!isPlaying()) {
			return;
		}

		// Update animation time position
		time += frame.elapsed().toMillis() * speed;

		// Check for end of animation
		if(time > duration) {
			if(isRepeating()) {
				// Cycle animation
				time = time % duration;
			}
			else {
				// Stop animation
				time = duration;
				animation.update(1);
				apply(Playable.State.STOP);
				return;
			}
		}

		// Update animation
		animation.update(time / (float) duration);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append(String.format("%s/%s", time, duration))
				.append("speed", speed)
				.append("repeat", repeat)
				.append(animation)
				.build();
	}
}
