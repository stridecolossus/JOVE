package org.sarge.jove.util;

/**
 * Defines a floating-point interpolator function.
 * @author Sarge
 */
@FunctionalInterface
public interface Interpolator {
	/**
	 * Interpolates a value.
	 * @param value Value to be interpolated
	 * @return Interpolated value
	 */
	float interpolate(float value);

	/**
	 * Linear interpolator.
	 */
	Interpolator LINEAR = x -> x;
	
	/**
	 * Sine interpolator.
	 */
	Interpolator SINE = x -> (1 - MathsUtil.cos(x * MathsUtil.PI)) / 2f;

	/**
	 * Factory for an interpolator that operates over the given range.
	 * @param start				Start value
	 * @param end				End value
	 * @param interpolator		Interpolator function
	 * @return Interpolator
	 */
	static Interpolator interpolator(float start, float end, Interpolator interpolator) {
		return x -> start + (end - start) * interpolator.interpolate(x);
	}
}
