package org.sarge.jove.util;

/**
 * An <i>interpolator</i> applies a mathematical function to a floating-point value to implement animations or <i>tweening</i> functionality.
 * TODO - doc
 * @author Sarge
 */
@FunctionalInterface
public interface Interpolator {
	/**
	 * Applies this interpolator to the given value.
	 * @param value Value to be interpolated
	 * @return Interpolated value
	 */
	float interpolate(float value);

	/**
	 * Interpolator that does nothing.
	 */
	Interpolator NONE = value -> value;

	/**
	 * Flip interpolator.
	 */
	Interpolator INVERT = value -> 1 - value;

	/**
	 * Cosine interpolator.
	 */
	Interpolator COSINE = value -> (1 - MathsUtil.cos(value * MathsUtil.PI)) / 2f;

	/**
	 * Smooth step (or <i>hermite</i>) interpolator - equivalent to the GLSL <code>mix</code> function.
	 * @see <a href="https://en.wikipedia.org/wiki/Smoothstep">Wikipedia</a>
	 */
	Interpolator SMOOTH = value -> value * value * (3 - 2 * value);

	/**
	 * Square interpolator.
	 */
	Interpolator SQUARED = value -> value * value;

	/**
	 * Creates an exponential interpolator.
	 * @param pow Exponent
	 * @return Exponential interpolator
	 */
	static Interpolator exponent(float pow) {
		return value -> (float) Math.pow(value, pow);
	}

	// https://www.febucci.com/2018/08/easing-functions/
	// https://gist.github.com/Fonserbc/3d31a25e87fdaa541ddf
	// http://paulbourke.net/miscellaneous/interpolation/

	/**
	 * Creates a linear (or <i>lerp</i>) interpolator over the given range.
	 * @param start		Range start
	 * @param end		Range end
	 * @return Linear interpolator
	 * @see #lerp(float, float, float)
	 */
	static Interpolator linear(float start, float end) {
		return value -> lerp(start, end, value);
	}

	/**
	 * Creates a compound interpolator over the given range.
	 * @param start				Range start
	 * @param end				Range end
	 * @param interpolator		Interpolator function
	 * @return Interpolator
	 * @see #lerp(float, float, float)
	 */
	static Interpolator of(float start, float end, Interpolator interpolator) {
		return value -> lerp(start, end, interpolator.interpolate(value));
	}

	/**
	 * Linearly interpolates a value over the given range.
	 * @param start		Range start
	 * @param end		Range end
	 * @param value		Value
	 * @return Interpolated value
	 * @see <a href="// https://en.wikipedia.org/wiki/Linear_interpolation">Wikipedia</a>
	 */
	static float lerp(float start, float end, float value) {
		return start + value * (end - start);
	}
}
