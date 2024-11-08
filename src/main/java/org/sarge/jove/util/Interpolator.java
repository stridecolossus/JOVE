package org.sarge.jove.util;

import org.sarge.jove.util.FloatSupport.FloatUnaryOperator;

/**
 * An <i>interpolator</i> is a floating-point function applied to a percentile value, used for animation, easing, etc.
 * <p>
 * Usage:
 * <pre>
 * // Perform a one-off linear interpolation over a range
 * float result = Interpolator.lerp(value, 1, 2);
 *
 * // Create an interpolator instance for the same purpose
 * Interpolator linear = Interpolator.linear(1, 2);
 *
 * // Create an easing functions
 * Interpolator in = Interpolator.QUADRATIC.range(1, 2);
 * Interpolator out = Interpolator.QUADRATIC.invert().range(1, 2);
 *
 * // Mix
 * Interpolator inout = Interpolator.mix(in, out);
 * </pre>
 * <p>
 * @author Sarge
 */
@FunctionalInterface
public interface Interpolator extends FloatUnaryOperator {
	/**
	 * Linear (or identity) interpolation.
	 */
	Interpolator LINEAR = t -> t;

	/**
	 * Quadratic (or squared) function.
	 */
	Interpolator QUADRATIC = t -> t * t;

	/**
	 * Common <i>smooth step</i> (or <i>hermite</i>) interpolator.
	 */
	Interpolator SMOOTH = t -> t * t * (3 - 2 * t);

	/**
	 * Creates an exponential interpolator.
	 * @param exp Exponent
	 * @return Exponential interpolator
	 */
	static Interpolator exponential(float exp) {
		return t -> (float) Math.pow(t, exp);
	}

	/**
	 * Inverts (or flips) this interpolator.
	 * @return Inverted interpolator
	 */
	default Interpolator invert() {
		return t -> 1 - apply(t);
	}

	// https://github.com/jMonkeyEngine/jmonkeyengine/blob/master/jme3-core/src/main/java/com/jme3/math/Easing.java

	/**
	 * Mirrors this interpolator.
	 * @return Mirror interpolator
	 */
	default Interpolator mirror() {
		return t -> {
			if(t <= MathsUtility.HALF) {
				return apply(t * 2);
			}
			else {
				return apply((1 - t) * 2);
			}
		};
	}

	/**
	 * Helper - Performs a linear interpolation.
	 * @param t			Percentile value 0..1
	 * @param start		Start value
	 * @param end		End value
	 * @return Interpolated value
	 */
	static float lerp(float t, float start, float end) {
		// TODO - Math.fma(end - start, t, start);
		return start + (end - start) * t;
	}

	/**
	 * Helper - Scales this interpolator over the given range.
	 * @param start		Start value
	 * @param end		End value
	 * @return Scaled interpolator
	 */
	default Interpolator range(float start, float end) {
		return t -> lerp(apply(t), start, end);
	}

	/**
	 * Helper - Creates a linear interpolator over the given range.
	 * @param start		Start value
	 * @param end		End value
	 * @return Linear interpolator
	 * @see #lerp(float, float, float)
	 */
	static Interpolator linear(float start, float end) {
		return t -> lerp(t, start, end);
	}

	/**
	 * Mixes two interpolators according to a given weighting.
	 * @param start			Start function
	 * @param end			End function
	 * @param weight		Weight 0..1
	 * @return Mix interpolator
	 */
	static Interpolator mix(Interpolator start, Interpolator end, float weight) {
		// TODO - Math.fma()
		return t -> (1 - weight) * start.apply(t) + weight * end.apply(t);
	}

	/**
	 * Mixes two interpolators according with equal weighting.
	 * @param start			Start function
	 * @param end			End function
	 * @return Mix interpolator
	 * @see #mix(Interpolator, Interpolator, float)
	 */
	static Interpolator mix(Interpolator start, Interpolator end) {
		return mix(start, end, MathsUtility.HALF);
	}
}
