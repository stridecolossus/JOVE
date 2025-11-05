package org.sarge.jove.foreign;

import java.lang.foreign.*;
import java.util.function.Function;
import java.util.stream.IntStream;

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

	/**
	 * Helper.
	 * Unmarshals a string array of the given length from off-heap memory.
	 * @param address		Off-heap memory
	 * @param length		Array length
	 * @return String array
	 */
	public static String[] array(MemorySegment address, int length) {
		final MemorySegment array = address.reinterpret(ValueLayout.ADDRESS.byteSize() * length);

		return IntStream
				.range(0, length)
				.mapToObj(n -> array.getAtIndex(ValueLayout.ADDRESS, n))
				.map(StringTransformer::unmarshal)
				.toArray(String[]::new);
	}
}
