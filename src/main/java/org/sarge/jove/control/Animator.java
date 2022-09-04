package org.sarge.jove.control;

import static org.sarge.lib.util.Check.notNull;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * An <i>animator</i> is a specialised playable for an {@link Animation} that is updated per-frame.
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
		 * @param animator Animator
		 * @return Whether this animation has finished
		 */
		boolean update(Animator animator);
	}

	private final Animation animation;
	private final Frame frame = new Frame();
	private float speed = 1;

	/**
	 * Constructor.
	 * @param animation Animation
	 */
	public Animator(Animation animation) {
		this.animation = notNull(animation);
	}

	/**
	 * @return Current time (epoch)
	 */
	public long time() {
		return frame.time().toEpochMilli();
	}

	/**
	 * @return Elapsed time (ms)
	 */
	public long elapsed() {
		return (long) (frame.elapsed().toMillis() * speed);
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
	public void state(State state) {
		// Delegate
		super.state(state);

		// Update frame tracker
		if(state == State.PLAY) {
			frame.start();
		}
		else {
			frame.end();
		}
	}

	@Override
	public void frame() {
		// Ignore if stopped or paused
		if(!isPlaying()) {
			return;
		}

		// Update animation
		frame.end();
		final boolean stop = animation.update(this);

		// Start next frame
		if(stop) {
			super.state(State.STOP);
		}
		else {
			frame.start();
		}
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append(animation)
				.append("speed", speed)
				.build();
	}
}
