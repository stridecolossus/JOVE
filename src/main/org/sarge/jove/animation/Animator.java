package org.sarge.jove.animation;

import org.sarge.jove.app.FrameListener;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Animator for an {@link Animation}.
 * <p>
 * An animator cycles a floating-point position over the given duration.
 * <p>
 * @author Sarge
 */
public class Animator extends AbstractPlayer implements FrameListener {
	private final Animation animation;
	private final long duration;
	private final AnimationInterpolator interpolator;

	private long time;
	private float pos;

	/**
	 * Constructor.
	 * @param animation		Animation managed by this animator
	 * @param duration		Animation duration (ms)
	 * @param interpolator	Position interpolator or <tt>null</tt> if none
	 */
	public Animator( Animation animation, long duration, AnimationInterpolator interpolator ) {
		Check.notNull( animation );
		Check.oneOrMore( duration );

		this.animation = animation;
		this.duration = duration;
		this.interpolator = interpolator;

		setRepeating( true );
	}

	/**
	 * @return Current animation time
	 */
	public long getTime() {
		return time;
	}

	/**
	 * @return Current animation position
	 */
	public float getPosition() {
		return pos;
	}

	@Override
	public void update( long t, long elapsed ) {
		// Ignore if not running
		if( !isPlaying() ) return;

		// Update animation time
		time += elapsed * super.getSpeed();

		if( !isRepeating() && ( time > duration ) ) {
			// Stop if past end and not repeating
			setState( State.STOPPED );
			time = duration;
		}
		else {
			// Quantize to duration
			time = time % duration;
		}

		// Calculate animation position
		if( interpolator != null ) {
			pos = interpolator.interpolate( time / (float) duration );
		}

		// Update animation
		animation.update( time, pos );
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
