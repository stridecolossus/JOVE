package org.sarge.jove.util;

/**
 * A <i>cosine function</i> is used to perform trigonometric calculations for rotations.
 * <p>
 * The purpose of this abstraction is to allow more performant implementations to be used where appropriate.
 * The {@link #DEFAULT} implementation delegates to the Java maths methods.
 * <p>
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
