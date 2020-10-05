package org.sarge.jove.util;

/**
 * Maths utilities.
 * @author Sarge
 */
public final class MathsUtil {
	public static final float ACCURACY = 0.0001f;

	/**
	 * PI as floating-point.
	 */
	public static final float PI = (float) Math.PI;

	/**
	 * Half-PI.
	 */
	public static final float HALF_PI = PI / 2f;

	/**
	 * Double PI.
	 */
	public static final float TWO_PI = 2f * PI;

	/**
	 * Degrees in a half-circle.
	 */
	public static final float HALF_CIRCLE_DEGREES = 180;

	/**
	 * Converts degrees to radians.
	 */
	public static final float DEGREES_TO_RADIANS = PI / HALF_CIRCLE_DEGREES;

	/**
	 * Converts radians to degrees.
	 */
	public static final float RADIANS_TO_DEGREES = HALF_CIRCLE_DEGREES / PI;

	/**
	 * Half value.
	 */
	public static final float HALF = 0.5f;

	private MathsUtil() {
	}

	/**
	 * @param a
	 * @param b
	 * @return Whether the given floating-point values are approximately equal
	 */
	public static boolean equals(float a, float b) {
		return Math.abs(a - b) < ACCURACY;
	}

	/**
	 * @param f Float
	 * @return Whether the given number is approximately zero
	 */
	public static boolean isZero(float f) {
		return Math.abs(f) < ACCURACY;
	}

	/**
	 * @param num Number
	 * @return Whether the given integer is even
	 */
	public static boolean isEven(int num) {
		return (num % 2) == 0;
	}

	/**
	 * @param value		Value
	 * @param mask		Bit-wise mask
	 * @return Whether the given value matches the specified bit-wise mask
	 * @throws IllegalArgumentException if the given mask is zero or negative
	 */
	public static boolean isMask(int value, int mask) {
		Check.oneOrMore(mask);
		return (value & mask) == mask;
	}

	/**
	 * @param mask		Mask
	 * @param bit		Bit index
	 * @return Whether the specified bit is set in the given integer mask
	 * @throws IllegalArgumentException if the given integer bit index is invalid
	 */
	public static boolean isBit(int mask, int bit) {
		Check.range(bit, 0, Integer.SIZE - 1);
		return isMask(mask, 1 << bit);
	}

	/**
	 * @param num Number
	 * @return Whether the given integer is a power-of-two
	 */
	public static boolean isPowerOfTwo(int num) {
		return (num > 0) && (num & (num - 1)) == 0;
	}

	/**
	 * @param radians Angle in radians
	 * @return Angle in degrees
	 */
	public static float toDegrees(float radians) {
		return radians * RADIANS_TO_DEGREES;
	}

	/**
	 * @param degrees Angle in degrees
	 * @return Angle in radians
	 */
	public static float toRadians(float degrees) {
		return degrees * DEGREES_TO_RADIANS;
	}

	/**
	 * @param f Value
	 * @return Square-root
	 */
	public static float sqrt(float f) {
		return (float) Math.sqrt(f);
	}

	/**
	 * Ensures the given angle is within the safe range on x86 processors.
	 * @param angle Angle (radians)
	 * @return Constrained angle
	 */
	private static float constrain(float angle) {
		// Clamp angle to two-PI space
		float result = angle % TWO_PI;

		// Clamp to PI space
		if(Math.abs(result) > PI) {
			result = result - TWO_PI;
		}

		// Clamp to half-PI space
		if(Math.abs(result) > HALF_PI) {
			result = PI - result;
		}

		return result;
	}

	/**
	 * @param angle Angle (radians)
	 * @return Sine of the given angle
	 */
	public static float sin(float angle) {
		final float reduced = constrain(angle);
		if(Math.abs(reduced) <= HALF_PI / 2f) {
			return (float) Math.sin(reduced);
		}
		else {
			return (float) Math.cos(HALF_PI - reduced);
		}
	}

	/**
	 * @param angle Angle (radians)
	 * @return Cosine of the given angle
	 */
	public static float cos(float angle) {
		return sin(angle + HALF_PI);
	}

	/**
	 * @param angle Angle (radians)
	 * @return Tangent of the given angle
	 */
	public static float tan(float angle) {
		return (float) Math.tan(angle);
	}

	/**
	 * @param angle Angle (radians)
	 * @return Arc-sine of the given angle
	 */
	public static float asin(float angle) {
		if(-1.0f < angle) {
			if(angle < 1.0f) {
				return (float) Math.asin(angle);
			}
			else {
				return HALF_PI;
			}
		}
		else {
			return -HALF_PI;
		}
	}

	/**
	 * @param angle Angle (radians)
	 * @return Arc-cosine of the given angle
	 */
	public static float acos(float angle) {
		if(-1.0f < angle) {
			if(angle < 1.0f) {
				return (float) Math.acos(angle);
			}
			else {
				return 0.0f;
			}
		}
		else {
			return PI;
		}
	}

	/**
	 * @param angle Angle (radians)
	 * @return Arc-tangent of the given angle
	 */
	public static float atan(float angle) {
		return (float) Math.atan(angle);
	}
}
