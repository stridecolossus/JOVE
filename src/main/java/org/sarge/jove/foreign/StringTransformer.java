package org.sarge.jove.foreign;

import java.lang.foreign.*;
import java.util.function.Function;

/**
 * The <i>string transformer</i> marshals a Java string to/from a native pointer to a null-terminated character-array.
 * @author Sarge
 */
public class StringTransformer extends DefaultTransformer<String> {
	@Override
	public MemorySegment marshal(String str, SegmentAllocator allocator) {
		return allocator.allocateFrom(str);
	}

	@Override
	public Function<MemorySegment, String> unmarshal() {
		return StringTransformer::unmarshal;
	}

	/**
	 * Unmarshals a string from the given off-heap memory.
	 * @param address Off-heap memory
	 * @return String
	 */
	public static String unmarshal(MemorySegment address) {
		return address.reinterpret(Integer.MAX_VALUE).getString(0L);
	}
}
