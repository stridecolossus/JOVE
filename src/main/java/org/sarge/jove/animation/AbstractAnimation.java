package org.sarge.jove.animation;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Partial implementation.
 * @author Sarge
 */
public abstract class AbstractAnimation implements Animation {
	private final long duration;
	private final float min, max;
	
	/**
	 * Constructor.
	 * @param duration		Animation duration (ms)
	 * @param min			Minimum value
	 * @param max			Maximum value
	 */
	protected AbstractAnimation( long duration, float min , float max ) {
		Check.oneOrMore( duration );
		if( min >= max ) throw new IllegalArgumentException( "Minimum cannot be larger than maximum" );
		
		this.duration = duration;
		this.min = min;
		this.max = max;
	}
	
	@Override
	public long getDuration() {
		return duration;
	}
	
	public float getMinimum() {
		return min;
	}
	
	@Override
	public float getMaximum() {
		return max;
	}
	
	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
