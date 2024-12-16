package org.sarge.jove.foreign;

import java.lang.foreign.*;
import java.util.function.Function;

/**
 * The <i>string native transformer</i> converts a Java string to a native pointer to a null-terminated character array, i.e. {@code char*}.
 * @author Sarge
 */
public record StringNativeTransformer() implements NativeTransformer<String, MemorySegment> {
	@Override
	public MemorySegment transform(String string, ParameterMode __, SegmentAllocator allocator) {
		if(string == null) {
			return MemorySegment.NULL;
		}
		else {
			return allocator.allocateFrom(string);
		}
	}

	@Override
	public Function<MemorySegment, String> returns() {
		return StringNativeTransformer::unmarshal;
	}

	/**
	 * Helper - Unmarshals a string from the given address.
	 * @param address Memory address
	 * @return String at the given address or {@code null} for a {@link MemorySegment#NULL} address
	 */
	public static String unmarshal(MemorySegment address) {
		return address.reinterpret(Integer.MAX_VALUE).getString(0L);
	}
}
