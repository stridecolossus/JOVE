package org.sarge.jove.util;

import java.util.stream.IntStream;

/**
 * A <i>mask</i> is a simple utility class representing an integer bit-field.
 * @author Sarge
 */
public record Mask(int mask) {
	/**
	 * Calculates the maximum unsigned integer value for the given number of bits.
	 * @param bits Number of bits
	 * @return Maximum unsigned value
	 */
	public static long unsignedMaximum(int bits) {
		return (1L << bits) - 1;
	}

	/**
	 * Maps the given bit index to an integer value, i.e. performs a left-shift.
	 * @param index Bit index
	 * @return Integer
	 */
	public static int toInteger(int index) {
		return 1 << index;
	}

	/**
	 * @param index Bit index
	 * @return Whether this mask contains the given bit
	 */
	public boolean contains(int value) {
		return (mask & value) == value;
	}

	/**
	 * @return The bits of this mask as a stream of indices
	 */
	public IntStream stream() {
		final int range = Integer.SIZE - Integer.numberOfLeadingZeros(mask);
		return IntStream
				.range(0, range)
				.filter(n -> contains(toInteger(n)));
	}
}
