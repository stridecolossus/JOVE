package org.sarge.jove.util;

import static java.util.stream.Collectors.toSet;

import java.lang.foreign.*;
import java.util.Set;
import java.util.stream.*;

import org.sarge.jove.foreign.Transformer;
import org.sarge.jove.util.IntEnum.ReverseMapping;

/**
 * An <i>enumeration mask</i> is a wrapper for a native integer bitfield representing a set of enumeration constants.
 * @see IntEnum
 * @param <E> Bit mask enumeration
 */
public record EnumMask<E extends IntEnum>(int bits) {
	/**
	 * Constructor given a set of constants.
	 * @param values Enumeration constants
	 * @see #reduce(Set)
	 */
	public EnumMask(Set<E> values) {
		this(reduce(values));
	}

	/**
	 * Reduces the given set of constants to an integer mask.
	 * @param values Enumeration constants
	 * @return Mask
	 */
	public static int reduce(Set<? extends IntEnum> values) {
		return values
				.stream()
				.mapToInt(IntEnum::value)
				.sum();
	}

	/**
	 * Convenience constructor given an array of enumeration constants.
	 * @param values Enumeration constants
	 */
	@SafeVarargs
	public EnumMask(E... values) {
		this(Set.of(values));
	}

	/**
	 * @param mask Bit mask
	 * @return Whether this bitfield contains the given mask
	 */
	public boolean contains(EnumMask<E> mask) {
		return contains(mask.bits);
	}

	/**
	 * @param mask Bit mask
	 * @return Whether this bitfield contains the given mask
	 */
	public boolean contains(int mask) {
		return (bits & mask) == mask;
	}

	/**
	 * Enumerates the constants of this bitfield.
	 * @param mapping Enumeration mapping
	 * @return Constants
	 * @see #stream(ReverseMapping)
	 */
	public Set<E> enumerate(ReverseMapping<E> mapping) {
		return this.stream(mapping).collect(toSet());
	}

	/**
	 * Enumerates the constants of this bitfield.
	 * @param mapping Enumeration mapping
	 * @return Constants
	 */
	public Stream<E> stream(ReverseMapping<E> mapping) {
		return stream(bits)
				.map(n -> 1 << n)
				.filter(this::contains)
				.mapToObj(mapping::map);
	}

	/**
	 * Helper - Enumerates the indices of the given bitfield up to the highest one bit.
	 * @param bits Bitfield
	 * @return Bit indices
	 */
	public static IntStream stream(int bits) {
		final int range = Integer.SIZE - Integer.numberOfLeadingZeros(bits);
		return IntStream.range(0, range);
	}

	@Override
	public String toString() {
		return Integer.toBinaryString(bits);
	}

	/**
	 * Native transformer for an enumeration mask.
	 */
	public static class EnumMaskTransformer implements Transformer<EnumMask<?>, Integer> {
		@Override
		public MemoryLayout layout() {
			return ValueLayout.JAVA_INT;
		}

		@Override
		public Integer marshal(EnumMask<?> e, SegmentAllocator allocator) {
			if(e == null) {
				return 0;
			}
			else {
				return e.bits();
			}
		}

		@Override
		public EnumMask<?> unmarshal(Integer value) {
			return new EnumMask<>(value);
		}
	}
}
