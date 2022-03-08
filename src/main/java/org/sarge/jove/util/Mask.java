package org.sarge.jove.util;

import java.util.stream.IntStream;

/**
 * A <i>mask</i> is a simple utility class representing a bit-field.
 * @author Sarge
 */
public record Mask(int mask) {
	/**
	 * @param value Bit-field
	 * @return Whether this mask contains <b>all</b> the bits specified by the given bit-field
	 */
	public boolean contains(int value) {
		return (mask & value) == value;
	}

	/**
	 * @param bit Bit index
	 * @return Whether this mask contains the given bit
	 */
	public boolean bit(int bit) {
		return contains(1 << bit);
	}

	/**
	 * @return The bits of this mask as a stream of integer masks
	 */
	public IntStream stream() {
		return IntStream
				.range(0, Integer.highestOneBit(mask))
				.map(bit -> 1 << bit)
				.filter(this::contains);
	}

	/**
	 * Calculates the maximum unsigned integer value for the given number of bits.
	 * @param bits Number of bits
	 * @return Maximum unsigned value
	 */
	public static long unsignedMaximum(int bits) {
		return (1L << bits) - 1;
	}
}
