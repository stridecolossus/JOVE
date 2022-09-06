package org.sarge.jove.control;

import static org.sarge.lib.util.Check.notNull;

import java.time.Duration;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * An <i>animator</i> is a specialised playable for an {@link Animation} that is interpolated over a duration.
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

	// Configuration
	private final Animation animation;
	private final long duration;
	private float speed = 1;
	private boolean repeat = true;

	// Animation state
	private final Frame frame;
	private long time;
	private float pos;

	/**
	 * Constructor.
	 * @param duration		Duration
	 * @param animation 	Animation
	 */
	public Animator(Duration duration, Animation animation) {
		this(new Frame(), duration, animation);
	}

	/**
	 * Test constructor.
	 * @param frame			Frame tracker
	 * @param duration		Duration
	 * @param animation		Animation
	 */
	protected Animator(Frame frame, Duration duration, Animation animation) {
		this.frame = notNull(frame);
		this.animation = notNull(animation);
		this.duration = duration.toMillis();
	}

	/**
	 * @return Duration of this animation
	 */
	public Duration duration() {
		return Duration.ofMillis(duration);
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
	 * @return Current time (epoch)
	 */
	public long time() {
		return frame.time().toEpochMilli();
	}

	/**
	 * @return Animation position (percentile)
	 */
	public float position() {
		return pos;
	}

	@Override
	public void play() {
		super.play();
		frame.start();
	}

	@Override
	public void pause() {
		super.pause();
		frame.end();
	}

	@Override
	public void stop() {
		super.stop();
		frame.end();
	}

	@Override
	public void update() {
		// Ignore if stopped or paused
		if(!isPlaying()) {
			return;
		}

		// Update animation time
		frame.end();
		time += frame.elapsed().toMillis() * speed;

		// Cycle or stop animation
		if(time > duration) {
			if(repeat) {
				time = time % duration;
			}
			else {
				super.stop();
				time = duration;
			}
		}

		// Update animation
		pos = time / (float) duration;
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
				.append("duration", duration)
				.append("speed", speed)
				.append("repeat", repeat)
				.build();
	}
}
