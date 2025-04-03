package org.sarge.jove.util;

import static java.util.stream.Collectors.toSet;

import java.lang.foreign.*;
import java.util.Set;

import org.sarge.jove.foreign.ReferenceTransformer;
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
	 */
	public EnumMask(Set<E> values) {
		this(reduce(values));
	}

	// TODO - why cannot this be done before this() in ctor?
	private static int reduce(Set<? extends IntEnum> values) {
		return values
				.stream()
				.mapToInt(IntEnum::value)
				.sum();
	}

	/**
	 * Constructor given an array of enumeration constants.
	 * @param values Enumeration constants
	 */
	@SafeVarargs
	public EnumMask(E... values) {
		this(Set.of(values));
	}

	/**
	 * @param mask Mask
	 * @return Whether this mask contains the given enumeration mask
	 */
	public boolean contains(EnumMask<E> mask) {
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

	/**
	 * Native transformer for an enumeration bitfield.
	 */
	@SuppressWarnings("rawtypes")
	public static class EnumMaskTransformer implements ReferenceTransformer<EnumMask, Integer> {
		@Override
		public MemoryLayout layout() {
			return ValueLayout.JAVA_INT;
		}

		@Override
		public Integer marshal(EnumMask mask, SegmentAllocator allocator) {
			if(mask == null) {
				return 0;
			}
			else {
				return mask.bits;
			}
		}

		@Override
		public Integer empty() {
			return 0;
		}

		@Override
		public EnumMask unmarshal(Integer bits) {
			return new EnumMask<>(bits);
		}
	}
}
