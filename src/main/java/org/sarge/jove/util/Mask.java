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
	 * @param bits Bit-field
	 * @return Whether this mask contains the given bit-field, i.e. is a super-set of the required bits
	 */
	public boolean contains(int bits) {
		return (mask & bits) == bits;
	}

	/**
	 * @return The bits of this mask as a stream of integers
	 */
	public IntStream stream() {
		final int range = Integer.SIZE - Integer.numberOfLeadingZeros(mask);
		return IntStream
				.range(0, range)
				.map(n -> 1 << n)
				.filter(this::contains);
	}
}
