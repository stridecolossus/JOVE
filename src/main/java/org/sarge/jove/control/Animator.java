package org.sarge.jove.control;

import static org.sarge.lib.util.Check.notNull;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * An <i>animator</i> is a specialised playable for an {@link Animation} that is updated per frame.
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
		 */
		void update(Animator animator);
	}

	private final Animation animation;
	private final Frame frame = new Frame();

	private float speed = 1;
	private long elapsed;
	private boolean repeat;

	/**
	 * Constructor.
	 * @param animation Animation
	 */
	public Animator(Animation animation) {
		this.animation = notNull(animation);
	}

	/**
	 * @return Animation position
	 */
	public float position() {
		return elapsed;
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
	public boolean isRepeating() {
		return repeat;
	}

	/**
	 * Sets whether this animator should repeat.
	 * @param repeat Whether repeating
	 */
	public void repeat(boolean repeat) {
		this.repeat = repeat;
	}

	@Override
	public void state(State state) {
		super.state(state);
		if(state == State.PLAY) {
			frame.start();
		}
	}

	@Override
	public void frame() {
		// Ignore if stopped or paused
		if(!isPlaying()) {
			return;
		}

		// Determine elapsed duration since start of frame
		elapsed = frame.end().toMillis(); // * speed;
		update(elapsed);

		// Update animation
		animation.update(this);

		// Start next frame
		if(isPlaying()) {
			frame.start();
		}
	}

	/**
	 * Updates this animator.
	 * @param elapsed Elapsed duration
	 */
	protected void update(long elapsed) {
		// Does nowt
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append(animation)
				.append("speed", speed)
				.append("repeat", repeat)
				.build();
	}
}
