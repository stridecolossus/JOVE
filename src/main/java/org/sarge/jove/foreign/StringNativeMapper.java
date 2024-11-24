package org.sarge.jove.foreign;

import java.lang.foreign.MemorySegment;
import java.util.*;
import java.util.function.Function;

/**
 * The <i>string native mapper</i> marshals a string as a native pointer to a null-terminated character array.
 * <p>
 * Note that since a Java string is an immutable type, the {@link #marshal(String, NativeContext)} method maintains a soft cache of marshalled strings.
 * <p>
 * @author Sarge
 */
public final class StringNativeMapper extends AbstractNativeMapper<String, MemorySegment> { // implements ReturnMapper<String, MemorySegment> {
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
	public MemorySegment marshal(String str, NativeContext context) {
		final var allocator = context.allocator();
		return cache.computeIfAbsent(str, allocator::allocateFrom);
	}

	@Override
	public Function<MemorySegment, String> returns(Class<? extends String> target) {
		// TODO - also cache?
		return StringNativeMapper::unmarshal;
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
