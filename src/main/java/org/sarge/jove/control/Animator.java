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

	protected final Animation animation;
	private final Frame frame = new Frame();
	private float speed = 1;
	private boolean repeat = true;

	/**
	 * Constructor.
	 * @param animation Animation
	 */
	public Animator(Animation animation) {
		this.animation = notNull(animation);
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

	/**
	 * @return Animation frame tracker
	 */
	public Frame frame() {
		return frame;
	}

	/**
	 * @return Animation time (ms)
	 */
	public float elapsed() {
		return this.frame().elapsed().toMillis() * speed;
	}

	@Override
	public void play() {
		super.play();
		frame.start();
	}

	@Override
	public void update() {
		// Ignore if stopped or paused
		if(!isPlaying()) {
			return;
		}

		// Update animation
		animation.update(this);

		// Start next frame
		if(isPlaying()) {
			frame.start();
		}
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
