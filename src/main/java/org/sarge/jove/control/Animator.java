package org.sarge.jove.control;

import static java.util.Objects.requireNonNull;
import static org.sarge.jove.util.Validation.requireOneOrMore;

import java.time.Duration;

/**
 * An <i>animator</i> is a specialised playable for an {@link Animation} interpolated over a given duration.
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
		void set(float pos);
	}

	// Configuration
	private final Animation animation;
	private final long duration;
	private final float scale;

	// State
	private float speed = 1;
	private boolean repeat = true;
	private long time;

	/**
	 * Constructor.
	 * @param animation 	Animation
	 * @param duration		Duration
	 */
	public Animator(Animation animation, Duration duration) {
		this.animation = requireNonNull(animation);
		this.duration = requireOneOrMore(duration.toNanos());
		//this.duration = requireOneOrMore(duration.toMillis());
		this.scale = 1f / this.duration; // .toMillis();
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
	 * @return Current animation position expressed as a percentile
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
		if(speed <= 0) {
			throw new IllegalArgumentException("Speed must be positive");
		}
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
	public void frame(Frame frame) {
		// Ignore if stopped or paused
		if(!isPlaying()) {
			return;
		}

		// Update animation position
//		final long elapsed = frame.elapsed().toMillis();
		final long elapsed = frame.elapsed().toNanos();

		if(elapsed == 0) {
			System.out.println("********* ZERO");
		}

//		if(!repeat && (elapsed > duration)) { // TODO - surely this should be time + elapsed > duration?
//			// Stop at end of duration if not repeating
//			state(State.STOPPED);
//			time = duration;
//			animation.set(1);
//		}
//		else {
			// Otherwise quantise animation time by duration
			time += elapsed * speed;
			time = time % duration;
			animation.set(time * scale);
//		}
	}

	@Override
	public String toString() {
		return String.format("Animator[state=%s time=%s animation=%s]", this.state(), this.time(), animation);
	}
}
