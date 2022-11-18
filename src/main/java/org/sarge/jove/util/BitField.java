package org.sarge.jove.util;

import static java.util.stream.Collectors.toSet;

import java.util.*;

import org.sarge.jove.util.IntegerEnumeration.ReverseMapping;

import com.sun.jna.*;

/**
 * A <i>bitfield</i> is a native integer representing a set of enumeration constants.
 * <p>
 * This allows enumerations that represent bitfields to be
 * type safety
 * clarity
 * <p>
 * example
 * <p>
 * @param <E> Bitfield enumeration
 */
public class BitField<E extends IntegerEnumeration> {
	private final int bits;

	/**
	 * Constructor.
	 * @param bits Bitfield
	 */
	public BitField(int bits) {
		this.bits = bits;
	}

	/**
	 * @return Native bitfield value
	 */
	public int bits() {
		return bits;
	}

	/**
	 * Builds a bitfield from the given enumeration constants.
	 * @param values Enumeration constants
	 * @param <E> Bitfield enumeration
	 * @return Bitfield
	 */
	public static <E extends IntegerEnumeration> BitField<E> reduce(Collection<E> values) {
		final int bits = values
				.stream()
				.mapToInt(IntegerEnumeration::value)
				.reduce(0, (a, b) -> a | b);

		return new BitField<>(bits);
	}

	/**
	 * @see #reduce(Collection)
	 */
	@SuppressWarnings("unchecked")
	public static <E extends IntegerEnumeration> BitField<E> reduce(E... values) {
		return reduce(Set.of(values));
	}

	/**
	 * Converts this bitfield to the corresponding enumeration constants.
	 * @param reverse Reverse mapping
	 * @return Enumeration constants
	 */
	public Set<E> enumerate(ReverseMapping<E> reverse) {
		return new Mask(bits)
    			.stream()
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
				(obj instanceof BitField<?> that) &&
				(this.bits == that.bits);
	}

	@Override
	public String toString() {
		return Integer.toBinaryString(bits);
	}

	/**
	 * JNA type converter for a bitfield.
	 */
	public static final TypeConverter CONVERTER = new TypeConverter() {
		@Override
		public Class<?> nativeType() {
			return Integer.class;
		}

		@Override
		public Object fromNative(Object nativeValue, FromNativeContext context) {
			if(nativeValue instanceof Integer n) {
				return new BitField<>(n);
			}
			else {
				return null;
			}
		}

		@Override
		public Object toNative(Object value, ToNativeContext context) {
			if(value instanceof BitField<?> bitfield) {
				return bitfield.bits;
			}
			else {
				return 0;
			}
		}
	};
}
