package org.sarge.jove.terrain;

import org.sarge.lib.util.Check;

/**
 * Weighting table entry.
 */
public class Weighting {
	private final float min, max;

	/**
	 * Constructor.
	 * @param min Range start
	 * @param max Range end
	 * @throws IllegalArgumentException if the range is invalid
	 */
	public Weighting( float min, float max ) {
		// TODO - Check.range( min, 0, 1 );
		Check.range( max, 0, 1 );
		if( max <= min ) throw new IllegalArgumentException( "Invalid range" );

		this.min = min;
		this.max = max;
	}

	/**
	 * Calculates the interpolated weighting for the given value.
	 * @param value Input value
	 * @return Interpolated weighting
	 */
	protected float getWeight( float value ) {
		if( value < min ) {
			return 0;
		}
		else
		if( value > max ) {
			return 0;
		}
		else {
			return ( value - min ) / ( max - min );
		}
	}

	@Override
	public String toString() {
		return min + "-" + max;
	}
}
