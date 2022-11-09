package org.sarge.jove.util;

/**
 * Trigonometric constants and utilities.
 * @author Sarge
 */
public interface Trigonometric {
	/**
	 * PI (or 180 degrees).
	 */
	float PI = (float) Math.PI;

	/**
	 * Half-PI (or 90 degrees).
	 */
	float HALF_PI = PI / 2;

	/**
	 * Double PI (or 360 degrees).
	 */
	float TWO_PI = 2 * PI;

	/**
	 * @param radians Angle in radians
	 * @return Angle in degrees
	 */
	static float toDegrees(float radians) {
		return (float) Math.toDegrees(radians);
	}

	/**
	 * @param degrees Angle in degrees
	 * @return Angle in radians
	 */
	static float toRadians(float degrees) {
		return (float) Math.toRadians(degrees);
	}
}
