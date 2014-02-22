package org.sarge.jove.util;

import java.util.Random;

/**
 * Randomiser utility methods.
 * @author Sarge
 */
public final class RandomUtil {
	public static final Random RANDOM = new Random();

	private RandomUtil() {
		// Utility class
	}

	/**
	 * @param range
	 * @return Floating-point value in the given range
	 */
	public static float nextFloat( float range ) {
		return RANDOM.nextFloat() * range;
	}

	/**
	 * @param min
	 * @param max
	 * @return Floating-point value in the range min..max
	 */
	public static float nextFloat( float min, float max ) {
		return min + nextFloat( max - min );
	}
}
