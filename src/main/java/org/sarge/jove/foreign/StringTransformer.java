package org.sarge.jove.foreign;

import java.lang.foreign.*;

/**
 * The <i>string transformer</i> marshals a Java string to/from a native pointer to a null-terminated character-array.
 * @author Sarge
 */
public class StringTransformer implements AddressTransformer<String, MemorySegment> {
	@Override
	public Object marshal(String str, SegmentAllocator allocator) {
		return allocator.allocateFrom(str);
	}

	@Override
	public String unmarshal(MemorySegment address) {
		return address.reinterpret(Integer.MAX_VALUE).getString(0L);
	}
}
