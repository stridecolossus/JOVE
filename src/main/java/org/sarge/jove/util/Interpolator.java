package org.sarge.jove.util;

import org.sarge.lib.element.Element;

/**
 * An <i>interpolator</i>
 * TODO
 *
 * P(0) = 0
 * P(1) = 1
 * P(t) = ???
 *
 *
 * @author Sarge
 */
@FunctionalInterface
public interface Interpolator {
	/**
	 * Applies this interpolator to the given value.
	 * @param value Value to be interpolated (assumes normalized)
	 * @return Interpolated value
	 */
	float interpolate(float t);

	/**
	 * Chains two interpolators.
	 * @param next Interpolator to apply <b>after</b> this interpolator
	 * @return Chained interpolator
	 */
	default Interpolator then(Interpolator next) {
		return t -> next.interpolate(this.interpolate(t));
	}

	/**
	 * Flips (or inverts) an interpolation function: <code>1 - P(t)</code>.
	 * @param delegate Delegate interpolator
	 * @return Flipped interpolator
	 */
	static Interpolator flip(Interpolator delegate) {
		return t -> 1 - delegate.interpolate(t);
	}

	// TODO
	//* @see <a href="https://en.wikipedia.org/wiki/Smoothstep">Wikipedia</a>
////https://www.febucci.com/2018/08/easing-functions/
//	Interpolator COSINE = value -> (1 - MathsUtil.cos(value * MathsUtil.PI)) / 2f;

	/**
	 * Linear interpolation, i.e. does nothing.
	 */
	Interpolator IDENTITY = t -> t;

	/**
	 * Quadratic (or squared) function.
	 */
	Interpolator QUADRATIC = t -> t * t;

	/**
	 * Cubic function.
	 */
	Interpolator CUBIC = t -> t * t * t;

	/**
	 * Convenience interpolator for the common <i>smooth step</i> (or <i>hermite</i>) interpolator.
	 * TODO - equivalent to mix(smooth start, smooth stop, ???)
	 */
	Interpolator SMOOTH = t -> t * t * (3 - 2 * t);

	/**
	 * Creates an interpolator that raises the parameter to the power of the given exponent.
	 * @param exp Exponent
	 * @return Power function
	 * @see Math#pow(double, double)
	 */
	static Interpolator pow(float exp) {
		return t -> (float) Math.pow(t, exp);
	}

	/**
	 * Creates a scaling interpolation function: <code>t * P(t)</code>.
	 * @param func Delegate function
	 * @return Scaling function
	 */
	static Interpolator scale(Interpolator func) {
		return t -> t * func.interpolate(t);
	}

	/**
	 * Creates an interpolator that mixes two functions according to a given weighting.
	 * TODO - explain weight -> b, doc, example
	 * @param start			Start function
	 * @param end			End function
	 * @param weight		Weight
	 * @return Compound interpolator
	 */
	static Interpolator mix(Interpolator start, Interpolator end, float weight) {
		// TODO - validate weight?
		return t -> (1 - weight) * start.interpolate(t) + weight * end.interpolate(t);
	}

	/**
	 * Helper - Creates a linear interpolator.
	 * @param start		Start value
	 * @param end		End value
	 * @return Linear interpolator
	 * @see #interpolate(float)
	 */
	static Interpolator linear(float start, float end) {
		return t -> interpolate(t, start, end);
	}

	/**
	 * Helper - Performs a one-off linear interpolation.
	 * @param t			Interpolator value
	 * @param start		Start value
	 * @param end		End value
	 * @return Interpolated value
	 */
	static float interpolate(float t, float start, float end) {
		return (1 - t) * start + t * end;
	}

	/**
	 * Loads an interpolator from the given element.
	 * @param e Element
	 * @return Interpolator
	 */
	static Interpolator load(Element e) {
		return switch(e.name()) {
			case "identity" -> Interpolator.IDENTITY;

			case "linear" -> {
				final float start = e.child("start").text().toFloat();
				final float end = e.child("end").text().toFloat();
				yield linear(start, end);
			}

			// TODO

			default -> throw e.exception("Unknown interpolator: " + e.name());
		};
	}
}
