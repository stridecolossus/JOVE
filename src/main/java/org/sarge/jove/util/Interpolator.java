package org.sarge.jove.util;

import static org.sarge.jove.util.MathsUtility.HALF;

/**
 * An <i>interpolator</i> is a floating-point function applied to a percentile value, used for animation, easing, etc.
 * TODO
 * - ease in ~ f(x)
 * - ease out ~ inverse
 * - in-out
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
		return value -> 1 - interpolate(1 - value);
	}
	// TODO - was just -> 1 - value
	// TODO ? EaseOut(t) = Flip(Square(Flip(t)))

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
	// TODO - ? equivalent to: EaseInOut(t) = Lerp(EaseIn(t), EaseOut(t), t)

	static Interpolator mirror(Interpolator delegate) {
		return value -> {
			if(value < HALF) {
				return delegate.interpolate(2 * value) * HALF;
			}
			else {
				return (2 - delegate.interpolate(2 * (1 - value))) * HALF;
			}
		};
	}
	// TODO - ? equivalent to: Spike/Mirror(t) = if(t < half) EaseIn(t / half) else EaseIn(Flip(t) / half);
}

// https://nicmulvaney.com/easing/?ref=blog.febucci.com#easeInQuad
// https://gist.github.com/Fonserbc/3d31a25e87fdaa541ddf?ref=blog.febucci.com
// https://easings.net/
// https://blog.febucci.com/2018/08/easing-functions/

