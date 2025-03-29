package org.sarge.jove.foreign;

import java.lang.foreign.*;
import java.util.function.Function;

/**
 * The <i>string native transformer</i> marshals a Java string to/from a native pointer to a null-terminated character-array.
 * @author Sarge
 */
public class StringNativeTransformer extends AbstractNativeTransformer<String> {
	@Override
	public Object marshal(String str, SegmentAllocator allocator) {
		return allocator.allocateFrom(str);
	}

	@Override
	public Function<MemorySegment, String> unmarshal() {
		return StringNativeTransformer::unmarshal;
	}

	/**
	 * Unmarshals a Java string from the given off-heap memory.
	 * @param address Off-heap address
	 * @return String
	 */
	public static String unmarshal(MemorySegment address) {
		return address.reinterpret(Integer.MAX_VALUE).getString(0L);
	}
}
