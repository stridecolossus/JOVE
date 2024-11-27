package org.sarge.jove.foreign;

import java.lang.foreign.*;
import java.util.*;
import java.util.function.Function;

/**
 * The <i>string transform</i> maps a Java string to a native <i>pointer-to-null-terminated character array</i>.
 * @author Sarge
 */
public final class StringNativeTransformer extends AbstractNativeTransformer<String, MemorySegment> {
	// TODO - soft cache: https://www.javaspecialists.eu/archive/Issue098-References.html
	// TODO - adapter class?
	private final Map<String, MemorySegment> cache = new WeakHashMap<>() {
		@Override
		public MemorySegment get(Object key) {
			final MemorySegment address = super.get(key);
			if(address == null) {
				return null;
			}
			else
			if(!address.scope().isAlive()) {
				remove(key);
				return null;
			}
			else {
				return address;
			}
		}
	};

	@Override
	public Class<String> type() {
		return String.class;
	}

	@Override
	public MemorySegment transform(String string, SegmentAllocator allocator) {
		return cache.computeIfAbsent(string, allocator::allocateFrom);
	}

	@Override
	public Function<MemorySegment, String> returns() {
		// TODO - also cache?
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
