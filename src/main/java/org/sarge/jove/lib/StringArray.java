package org.sarge.jove.lib;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.util.*;

/**
 * A <i>string array</i> maps an array of strings to a native pointer-to-array of null-terminated character arrays.
 * @author Sarge
 */
public class StringArray {
	private String[] array;
	private MemorySegment address;

	/**
	 * Constructor.
	 * @param array String array
	 */
	public StringArray(String[] array) {
		this.array = Arrays.copyOf(array, array.length);
	}

	public StringArray(Collection<String> strings) {
		this.array = strings.toArray(String[]::new);
	}

	private StringArray(MemorySegment address) {
		this.address = requireNonNull(address);
	}

	protected MemorySegment allocate(SegmentAllocator allocator) {
		if(address == null) {
			address = allocator.allocate(ADDRESS, array.length);
    		for(int n = 0; n < array.length; ++n) {
    			final MemorySegment str = allocator.allocateFrom(array[n]);
    			address.setAtIndex(ADDRESS, n, str);
    		}
		}
		return address;
	}

	/**
	 * Retrieves the string-array from this carrier.
	 * @param length Array length
	 * @return String array
	 */
	public String[] array(int length) {
		if(array == null) {
    		final MemorySegment address = this.address.reinterpret(length * ADDRESS.byteSize());
    		this.array = new String[length];
    		Arrays.setAll(array, n -> StringNativeMapper.unmarshal(address.getAtIndex(ADDRESS, n)));
		}
		return array;
	}
	// TODO - fromNative ~ scope

	@Override
	public int hashCode() {
		return Objects.hash(array, address);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof StringArray that) &&
				(Objects.equals(this.address, that.address) || Arrays.equals(this.array, that.array));
	}

	@Override
	public String toString() {
		return String.format("StringArray[%s]", array == null ? "unallocated" : String.valueOf(array.length));
	}

	/**
	 * Native mapper for a string array.
	 */
	public static class StringArrayNativeMapper extends DefaultNativeMapper<StringArray, MemorySegment> {
		/**
		 * Constructor.
		 */
		public StringArrayNativeMapper() {
			super(StringArray.class, ValueLayout.ADDRESS);
		}

		@Override
		public MemorySegment toNative(StringArray array, NativeContext context) {
			return array.allocate(context.allocator());
		}

		@Override
		public StringArray fromNative(MemorySegment address, Class<? extends StringArray> type) {
			return new StringArray(address);
		}
	}
}
