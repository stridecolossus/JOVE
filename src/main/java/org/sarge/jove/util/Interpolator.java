package org.sarge.jove.util;

import java.util.Iterator;

import org.sarge.jove.util.FloatSupport.FloatUnaryOperator;
import org.sarge.lib.element.Element;
import org.sarge.lib.element.Element.Content;

/**
 * An <i>interpolator</i>
 * TODO
 *
 * P(0) = 0
 * P(1) = 1
 * P(t) = ???
 *
	// TODO
	//* @see <a href="https://en.wikipedia.org/wiki/Smoothstep">Wikipedia</a>
////https://www.febucci.com/2018/08/easing-functions/
//	Interpolator COSINE = value -> (1 - MathsUtil.cos(value * MathsUtil.PI)) / 2f;
 *
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

	/**
	 * Helper - Performs a linear interpolation.
	 * @param t			Percentile value 0..1
	 * @param start		Start value
	 * @param end		End value
	 * @return Interpolated value
	 */
	static float lerp(float t, float start, float end) {
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
		return mix(start, end, MathsUtil.HALF);
	}

	/**
	 * Loads an interpolator from the given element.
	 * @param e Element
	 * @return Interpolator
	 */
	static Interpolator load(Element e) {
		return switch(e.name()) {
			case "identity" -> Interpolator.LINEAR;

			case "quadratic", "square", "squared" -> Interpolator.QUADRATIC;

			case "smooth", "smoothstep" -> Interpolator.SMOOTH;

			case "linear" -> {
				final float start = e.child("start").text().toFloat();
				final float end = e.child("end").text().toFloat();
				yield linear(start, end);
			}

			case "mix" -> {
				// TODO - nasty 1. uses iterator 2. assumes element order
				final Iterator<Element> itr = e.children().iterator();
				final Interpolator start = load(itr.next());
				final Interpolator end = load(itr.next());
				final float weight = e.optional("weight").map(Element::text).map(Content::toFloat).orElse(MathsUtil.HALF);
				yield mix(start, end, weight);
			}

			default -> throw e.exception("Unknown interpolator: " + e.name());
		};
	}
}
