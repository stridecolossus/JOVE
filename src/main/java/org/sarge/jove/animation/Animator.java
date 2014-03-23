package org.sarge.jove.animation;

import org.sarge.jove.app.FrameListener;
import org.sarge.jove.util.Interpolator;
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
	private final Interpolator interpolator;

	private long time;

	/**
	 * Constructor.
	 * @param animation		Animation managed by this animator
	 * @param duration		Animation duration (ms)
	 * @param interpolator	Position interpolator or <tt>null</tt> if none
	 */
	public Animator( Animation animation, long duration, Interpolator interpolator ) {
		Check.notNull( animation );
		Check.oneOrMore( duration );

		this.animation = animation;
		this.duration = duration;
		this.interpolator = interpolator;

		setRepeating( true );
	}

	/**
	 * Constructor for an animation with no interpolation.
	 * @param animation		Animation managed by this animator
	 * @param duration		Animation duration (ms)
	 */
	public Animator( Animation animation, long duration ) {
		this( animation, duration, null );
	}

	/**
	 * @return Current animation time
	 */
	public long getTime() {
		return time;
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

		// Apply interpolation
		final float interpolated;
		if( interpolator == null ) {
			interpolated = time;
		}
		else {
			interpolated = interpolator.interpolate( 0, duration, time );
		}

		// Clamp to animation range
		final float pos = animation.getMinimum() + ( interpolated / duration ) * ( animation.getMaximum() - animation.getMinimum() );

		// Update animation
		animation.update( pos );
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
