package org.sarge.jove.geometry;

import org.sarge.jove.util.MathsUtility;

/**
 * A <i>cosine function</i> calculates the sine-cosine of a given angle.
 * @author Sarge
 */
public interface CosineFunction {
	/**
	 * @param angle Angle (radians)
	 * @return Cosine of the angle
	 */
	float cos(float angle);

	/**
	 * @param angle Angle (radians)
	 * @return Sine of the angle
	 */
	default float sin(float angle) {
		return cos(angle - MathsUtility.HALF_PI);
	}

	/**
	 * Default implementation that delegates to the built-in JVM methods.
	 */
	CosineFunction DEFAULT = new CosineFunction() {
		@Override
		public float sin(float angle) {
			return (float) Math.sin(angle);
		}

		@Override
		public float cos(float angle) {
			return (float) Math.cos(angle);
		}
	};
}
