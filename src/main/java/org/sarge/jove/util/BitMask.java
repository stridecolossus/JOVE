package org.sarge.jove.util;

import static java.util.stream.Collectors.toSet;

import java.util.*;

import org.sarge.jove.util.IntEnum.ReverseMapping;

/**
 * A <i>bit mask</i> is a wrapper for a native integer bitfield representing a set of enumeration constants.
 * <p>
 * This class allows enumeration bit masks in API methods and structures to be represented in a more type-safe manner, i.e. rather than a simple integer.
 * <p>
 * @see IntEnum
 * @param <E> Bit mask enumeration
 */
public record BitMask<E extends IntEnum>(int bits) {
	/**
	 * Creates a bit mask from the given array.
	 * @param <E> Bit mask enumeration
	 * @param values Enumeration array
	 * @return Bit mask
	 * @see #BitMask(Collection)
	 */
	@SafeVarargs
	public static <E extends IntEnum> BitMask<E> of(E... values) {
		return new BitMask<>(Set.of(values));
	}

	/**
	 * Constructor.
	 * @param bits Bit mask
	 */
	public BitMask {
		// Empty
	}

	/**
	 * Constructor.
	 * @param values Enumeration
	 */
	public BitMask(Collection<E> values) {
		this(reduce(values));
	}

	private static int reduce(Collection<? extends IntEnum> values) {
		return values.stream().mapToInt(IntEnum::value).sum();
	}

	/**
	 * @param mask Mask
	 * @return Whether this mask contains the given enumeration mask
	 */
	public boolean contains(BitMask<E> mask) {
		return BitField.contains(bits, mask.bits);
	}

	/**
	 * @param value Enumeration constant
	 * @return Whether this mask contains the given constant
	 */
	public boolean contains(E value) {
		return BitField.contains(bits, value.value());
	}

	/**
	 * Converts this mask to the corresponding enumeration.
	 * @param reverse Reverse mapping
	 * @return Enumeration constants
	 */
	public Set<E> enumerate(ReverseMapping<E> reverse) {
		return new BitField(bits)
    			.stream()
    			.map(BitField::map)
    			.mapToObj(reverse::map)
    			.collect(toSet());
	}

	@Override
	public String toString() {
		return Integer.toBinaryString(bits);
	}
}
