package org.sarge.jove.util;

/**
 * Interpolates a value.
 * @author Sarge
 */
public abstract class Interpolator {
	/**
	 * Linear interpolator.
	 */
	public static final Interpolator LINEAR = new Interpolator() {
		@Override
		protected float interpolate( float pos ) {
			return pos;
		}
	};

	/**
	 * Sine interpolator.
	 */
	public static final Interpolator SINE = new Interpolator() {
		@Override
		protected float interpolate( float value ) {
			return ( 1 - MathsUtil.cos( value * MathsUtil.PI ) ) / 2f;
		}
	};

	/**
	 * Interpolates the given value.
	 * @param value Initial value
	 * @return Interpolated value
	 */
	protected abstract float interpolate( float value );

	/**
	 * Interpolates the given value between the two positions.
	 * @param start		Start position
	 * @param end		End
	 * @param value		Initial value
	 * @return Interpolated value
	 */
	public float interpolate( float start, float end, float value ) {
		return start + ( end - start ) * interpolate( value );
	}
}
