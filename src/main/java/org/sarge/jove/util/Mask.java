package org.sarge.jove.util;

import java.util.stream.IntStream;

/**
 * A <i>mask</i> is a simple utility class representing a bit-field.
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
	 * @return Whether this mask contains <b>all</b> the bits specified by the given bit-field
	 */
	public boolean contains(int bits) {
		return (mask & bits) == bits;
	}

	/**
	 * @param bit Bit index
	 * @return Whether this mask contains the given bit
	 */
	public boolean bit(int bit) {
		return contains(1 << bit);
	}

	/**
	 * @return The bits of this mask as a stream of integers
	 */
	public IntStream stream() {

		// TODO - this is not right, e.g. for 0b100 (4) ->
		// stream should be bit INDICES (and also using bit() method) -> 0 1 2
		// but will actually be 0..4 -> i.e. 0 1 2 3!!!
		// only works because duplicates reduce to a set

		return IntStream
				.range(0, Integer.highestOneBit(mask))
				.map(bit -> 1 << bit)
				.filter(this::contains);
	}
}
