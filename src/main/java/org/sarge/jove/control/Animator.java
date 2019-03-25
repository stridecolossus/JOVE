package org.sarge.jove.control;

import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import org.sarge.jove.common.Frame;
import org.sarge.jove.control.Player.Playable;
import org.sarge.jove.control.Player.State;
import org.sarge.jove.geometry.Rotation.MutableRotation;
import org.sarge.jove.util.MathsUtil;
import org.sarge.lib.util.AbstractObject;

/**
 * An <i>animator</i> cycles a value over a given period.
 * @author Sarge
 */
public class Animator extends AbstractObject implements Playable, Frame.Listener {
	/**
	 * Animation.
	 */
	@FunctionalInterface
	public interface Animation {
		/**
		 * Notifies that the animation has been updated.
		 * @param animator Updated animator
		 */
		void update(Animator animator);
	}

	/**
	 * Creates an animation for the given rotation.
	 * @param rot Rotation
	 * @return Rotation animation
	 */
	public static Animation rotation(MutableRotation rot) {
		return animator -> rot.angle(animator.position() * MathsUtil.TWO_PI);
	}

	private final long duration;
	private final Animation animation;

	private long time;
	private float speed = 1;
	private Player.State state = Player.State.STOP;
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
	 * @return Current time position
	 */
	public long time() {
		return time;
	}

	/**
	 * @return Animation position 0..1
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
	public void update(Frame frame) {
		// Ignore if stopped or paused
		if(!isPlaying()) {
			return;
		}

		// Update time position
		time += frame.elapsed() * speed;

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
}