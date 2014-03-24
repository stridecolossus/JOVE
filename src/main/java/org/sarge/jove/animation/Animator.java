package org.sarge.jove.animation;

import org.sarge.jove.app.FrameListener;
import org.sarge.jove.util.Interpolator;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Animator for an {@link Animation}.
 * @author Sarge
 */
public class Animator extends AbstractPlayer implements FrameListener {
	private final Animation animation;
	private final Interpolator interpolator;

	private long time;

	/**
	 * Constructor.
	 * @param animation		Animation managed by this animator
	 * @param interpolator	Position interpolator or <tt>null</tt> if none
	 */
	public Animator( Animation animation, Interpolator interpolator ) {
		Check.notNull( animation );

		this.animation = animation;
		this.interpolator = interpolator;

		setRepeating( true );
	}

	/**
	 * Constructor for an animation with linear interpolation.
	 * @param animation Animation managed by this animator
	 * @see Interpolator#LINEAR
	 */
	public Animator( Animation animation ) {
		this( animation, Interpolator.LINEAR );
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

		// Quantize to animation duration and check whether finished
		final long duration = animation.getDuration();
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
		final float pos = interpolator.interpolate( animation.getMinimum(), animation.getMaximum(), time / (float) duration );

		// Update animation
		animation.update( pos );
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
