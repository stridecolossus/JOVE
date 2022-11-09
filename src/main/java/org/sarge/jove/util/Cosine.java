package org.sarge.jove.util;

/**
 * Cosine function.
 * @see <a href="https://en.wikipedia.org/wiki/Sine">Wikipedia</a>
 * @author Sarge
 */
public interface Cosine extends Trigonometric {
	/**
	 * @param angle Angle (radians)
	 * @return Cosine of the given angle
	 */
	float cos(float angle);

	/**
	 * @param angle Angle (radians)
	 * @return Sine of the given angle
	 */
	default float sin(float angle) {
		return cos(angle - HALF_PI);
	}

	/**
	 * Default implementation that delegates to Java maths.
	 */
	Cosine DEFAULT = new Cosine() {
		@Override
		public float cos(float angle) {
			return (float) Math.cos(angle);
		}

		@Override
		public float sin(float angle) {
			return (float) Math.sin(angle);
		}
	};
}
