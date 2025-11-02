package org.sarge.jove.foreign;

import java.lang.foreign.*;
import java.util.function.Function;

/**
 * The <i>string transformer</i> marshals a Java string to/from a native pointer to a null-terminated character-array.
 * @author Sarge
 */
public class StringTransformer implements Transformer<String, MemorySegment> {
	@Override
	public MemorySegment marshal(String str, SegmentAllocator allocator) {
		// TODO - soft(?) cache
		return allocator.allocateFrom(str);
	}

	@Override
	public Function<MemorySegment, String> unmarshal() {
		return StringTransformer::unmarshal;
	}

	/**
	 * Helper.
	 * Unmarshals an off-heap string.
	 * @param address Off-heap memory
	 * @return Unmarshalled string
	 */
	public static String unmarshal(MemorySegment address) {
		return address.reinterpret(Integer.MAX_VALUE).getString(0L);
	}
}
