package org.sarge.jove.control;

import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.control.Player.Playable;
import org.sarge.jove.control.Player.State;

/**
 * An <i>animator</i> cycles a value over a given period.
 * @author Sarge
 */
public class Animator implements Playable, FrameTracker.Listener {
	/**
	 * An <i>animation</i> is updated by this animator.
	 */
	@FunctionalInterface
	public interface Animation {
		/**
		 * Notifies that the animation has been updated.
		 * @param animator Updated animator
		 */
		void update(Animator animator);
	}

	private final long duration;
	private final Animation animation;

	private long time;
	private State state = State.STOP;
	private float speed = 1;
	private boolean repeat;

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
	 * @return Current time position within the animation duration
	 */
	public long time() {
		return time;
	}

	/**
	 * Calculates the animation <i>position</i> as a floating-point value in the range zero to one.
	 * @return Animation position
	 */
	public float position() {
		return time / (float) duration;
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
	public void apply(State state) {
		this.state = state;
	}

	@Override
	public boolean isPlaying() {
		return state == State.PLAY;
	}

	@Override
	public void setRepeating(boolean repeat) {
		this.repeat = repeat;
	}

	@Override
	public void update(FrameTracker tracker) {
		// Ignore if stopped or paused
		if(!isPlaying()) {
			return;
		}

		// Update time position
		time += tracker.elapsed() * speed;

		// Check for completed animation
		if(time > duration) {
			if(repeat) {
				time = time % duration;
			}
			else {
				time = duration;
				state = Player.State.STOP;
			}
		}

		// Update animation
		animation.update(this);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append(state)
				.append(String.format("%d/%d", time, duration))
				.build();
	}
}
