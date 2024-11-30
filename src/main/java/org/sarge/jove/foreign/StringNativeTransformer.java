package org.sarge.jove.foreign;

import java.lang.foreign.*;
import java.util.function.Function;

/**
 * The <i>string transform</i> maps a Java string to a native <i>pointer-to-null-terminated character array</i>.
 * @author Sarge
 */
public final class StringNativeTransformer extends AbstractNativeTransformer<String, MemorySegment> {
	@Override
	public Class<String> type() {
		return String.class;
	}

	@Override
	public MemorySegment transform(String string, SegmentAllocator allocator) {
		return allocator.allocateFrom(string);
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
		return address.reinterpret(Integer.MAX_VALUE).getString(0);
	}
}
