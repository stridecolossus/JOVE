package org.sarge.jove.util;

/**
 * An <i>interpolator</i> is a floating-point function applied to a percentile value, used for animation, easing, etc.
 * @author Sarge
 */
@FunctionalInterface
public interface Interpolator {
	/**
	 * Interpolates the given value.
	 * Assumes but does not enforce that the {@link #value} is in the range {@code 0..1}.
	 * @param value Value to interpolate
	 * @return Interpolated value
	 */
	float interpolate(float value);

	/**
	 * Identity interpolation, i.e. does nothing.
	 */
	Interpolator IDENTITY = value -> value;

	/**
	 * Quadratic (or square) interpolator.
	 * @see #exponential(float)
	 */
	Interpolator QUADRATIC = value -> value * value;

	/**
	 * Common <i>smooth step</i> (or <i>hermite</i>) interpolator.
	 * TODO - refs
	 */
	Interpolator SMOOTH = value -> value * value * (3 - 2 * value);

	/**
	 * @return Inverse of this interpolator
	 */
	default Interpolator invert() {
		return value -> 1 - interpolate(value);
	}

	/**
	 * Combines this and the given interpolator.
	 * @param after Interpolator to apply after this interpolator
	 * @return Combined interpolator
	 */
	default Interpolator andThen(Interpolator after) {
		return value -> after.interpolate(interpolate(value));
	}

	/**
	 * Composes this and the given interpolator.
	 * @param before Interpolator to apply before this interpolator
	 * @return Composed interpolator
	 */
	default Interpolator compose(Interpolator before) {
		return value -> interpolate(before.interpolate(value));
	}

	/**
	 * Creates a linear interpolator over the given range.
	 * @param min		Minimum
	 * @param max		Maximum
	 * @return Linear interpolator
	 */
	static Interpolator linear(float min, float max) {
		return value -> min + (max - min) * value;
	}

	/**
	 * Creates an exponential interpolator.
	 * @param exponent Exponent
	 * @return Exponential interpolator
	 * @see Math#pow(double, double)
	 */
	static Interpolator exponential(float exponent) {
		return value -> (float) Math.pow(value, exponent);
	}

	/**
	 * Mixes two interpolators according to the given weighting.
	 * @param weight Weighting
	 * @return Mix interpolator
	 */
	static Interpolator mix(Interpolator start, Interpolator end, Percentile weight) {
		return value -> (1 - weight.value()) * start.interpolate(value) + weight.value() * end.interpolate(value);
	}
}

//
//	// https://github.com/jMonkeyEngine/jmonkeyengine/blob/master/jme3-core/src/main/java/com/jme3/math/Easing.java
//
//	/**
//	 * Mirrors this interpolator.
//	 * @return Mirror interpolator
//	 */
//	default Interpolator mirror() {
//		return t -> {
//			if(t <= MathsUtility.HALF) {
//				return apply(t * 2);
//			}
//			else {
//				return apply((1 - t) * 2);
//			}
//		};
//	}
