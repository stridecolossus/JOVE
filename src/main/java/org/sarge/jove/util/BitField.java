package org.sarge.jove.util;

import java.util.stream.IntStream;

/**
 * A <i>bitfield</i> is a utility class representing an integer mask.
 * @author Sarge
 */
public record BitField(int mask) {
	/**
	 * Calculates the maximum unsigned integer value for the given number of bits.
	 * @param bits Number of bits
	 * @return Maximum unsigned value
	 */
	public static long unsignedMaximum(int bits) {
		return (1L << bits) - 1;
	}

	/**
	 * Maps the given bit index to the corresponding integer value, i.e. performs a left-shift.
	 * @param index Bit index 0..31
	 * @return Integer value
	 */
	public static int map(int index) {
		return 1 << index;
	}

	/**
	 * Helper.
	 * @param bitfield		Bitfield
	 * @param value			Value
	 * @return Whether a bitfield contains the given value
	 */
	public static boolean contains(int bitfield, int value) {
		return (bitfield & value) == value;
	}

	/**
	 * @return The positive bits of this mask as a stream of indices in the range 0..31, lowest first
	 */
	public IntStream stream() {
		final int range = Integer.SIZE - Integer.numberOfLeadingZeros(mask);
		return IntStream.range(0, range).filter(this::contains);
	}

	private boolean contains(int index) {
		return Integer.lowestOneBit(mask >>> index) == 1;
	}

	@Override
	public String toString() {
		return Integer.toBinaryString(mask);
	}
}
