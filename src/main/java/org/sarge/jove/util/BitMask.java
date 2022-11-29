package org.sarge.jove.util;

import static java.util.stream.Collectors.toSet;

import java.util.*;

import org.sarge.jove.util.IntEnum.ReverseMapping;

import com.sun.jna.*;

/**
 * A <i>bit mask</i> is a wrapper for a native integer representing a set of enumeration constants.
 * <p>
 * This class allows enumeration bit masks in API methods and structures to be represented in a more type-safe manner, i.e. rather than a simple integer.
 * <p>
 * The {@link #reduce(Collection)} method creates a bit mask from a collection of enumeration constants and {@link #enumerate(ReverseMapping)} performs the inverse operation.
 * <p>
 * @see IntEnum
 * @param <E> Bit mask enumeration
 */
public final class BitMask<E extends IntEnum> {
	private final int bits;

	/**
	 * Constructor.
	 * @param bits Bitfield
	 */
	public BitMask(int bits) {
		this.bits = bits;
	}

	/**
	 * @return Native bitfield value
	 */
	public int bits() {
		return bits;
	}

	/**
	 * Builds a mask from the given enumeration constants.
	 * @param values Enumeration constants
	 * @param <E> Bitfield enumeration
	 * @return New bit mask
	 */
	public static <E extends IntEnum> BitMask<E> reduce(Collection<E> values) {
		final int bits = values
				.stream()
				.mapToInt(IntEnum::value)
				.reduce(0, (a, b) -> a | b);

		return new BitMask<>(bits);
	}

	/**
	 * @see #reduce(Collection)
	 */
	@SuppressWarnings("unchecked")
	public static <E extends IntEnum> BitMask<E> reduce(E... values) {
		return reduce(Set.of(values));
	}

	/**
	 * @param mask Mask
	 * @return Whether this enumeration contains the given mask
	 */
	public boolean contains(BitMask<E> mask) {
		return (bits & mask.bits) == mask.bits;
	}

	/**
	 * Converts this mask to the corresponding enumeration constants.
	 * @param reverse Reverse mapping
	 * @return Enumeration constants
	 */
	public Set<E> enumerate(ReverseMapping<E> reverse) {
		return new Mask(bits)
    			.stream()
    			.map(Mask::toInteger)
    			.mapToObj(reverse::map)
    			.collect(toSet());
	}

	@Override
	public int hashCode() {
		return bits;
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof BitMask<?> that) &&
				(this.bits == that.bits);
	}

	@Override
	public String toString() {
		return Integer.toBinaryString(bits);
	}

	/**
	 * JNA type converter for a bit mask.
	 */
	public static final TypeConverter CONVERTER = new TypeConverter() {
		@Override
		public Class<?> nativeType() {
			return Integer.class;
		}

		@Override
		public Object fromNative(Object nativeValue, FromNativeContext context) {
			if(nativeValue instanceof Integer n) {
				return new BitMask<>(n);
			}
			else {
				return null;
			}
		}

		@Override
		public Object toNative(Object value, ToNativeContext context) {
			if(value instanceof BitMask<?> bitfield) {
				return bitfield.bits;
			}
			else {
				return 0;
			}
		}
	};
}
