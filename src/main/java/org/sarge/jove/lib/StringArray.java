package org.sarge.jove.lib;

import static java.lang.foreign.MemorySegment.NULL;
import static java.lang.foreign.ValueLayout.ADDRESS;

import java.lang.foreign.*;
import java.util.Arrays;

import org.sarge.jove.lib.NativeMapper.ReturnMapper;

/**
 * A <i>string array</i> maps an array of strings to a native pointer-to-array of null-terminated character arrays.
 * @author Sarge
 */
public class StringArray extends Address {
	/**
	 * Constructor.
	 * @param array		String array
	 * @param arena		Arena
	 */
	public StringArray(String[] array, Arena arena) {
		final MemorySegment address = arena.allocate(ADDRESS, array.length);
		for(int n = 0; n < array.length; ++n) {
			final MemorySegment str = arena.allocateFrom(array[n]);
			address.setAtIndex(ADDRESS, n, str);
		}
		super(address);
	}

	/**
	 * Constructor for a string-array returned from the native layer.
	 * @param address Address
	 */
	protected StringArray(MemorySegment address) {
		super(address.asReadOnly());
	}

	/**
	 * Retrieves the string-array from this carrier.
	 * @param length Array length
	 * @return String array
	 */
	public String[] array(int length) {
		final MemorySegment address = this.address().reinterpret(length * ADDRESS.byteSize());
		final String[] array = new String[length];
		Arrays.setAll(array, n -> StringNativeMapper.fromNative(address.getAtIndex(ADDRESS, n)));
		return array;
	}

	/**
	 * Native mapper for a string array.
	 */
	public static class StringArrayNativeMapper extends AddressNativeMapper<StringArray> implements ReturnMapper<MemorySegment> {
		/**
		 * Constructor.
		 */
		public StringArrayNativeMapper() {
			super(StringArray.class);
		}

		@Override
		public Object toNativeNull(Class<?> type) {
			return NULL;
		}

		@Override
		public StringArray fromNative(MemorySegment address, Class<?> type) {
			if(NULL.equals(address)) {
				return null;
			}
			else {
				return new StringArray(address);
			}
		}
	}
}
