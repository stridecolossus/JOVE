package org.sarge.jove.lib;

import java.lang.foreign.MemorySegment;
import java.util.*;

import org.sarge.jove.lib.NativeMapper.ReturnMapper;

/**
 * The <i>string native mapper</i> marshals a string as a native pointer to a null-terminated character array.
 * <p>
 * Note that since a Java string is an immutable type, the {@link #marshal(String, NativeContext)} method maintains a soft cache of marshalled strings.
 * <p>
 * @author Sarge
 */
public final class StringNativeMapper extends AbstractNativeMapper<String> implements ReturnMapper<String, MemorySegment> {
	// TODO - soft cache: https://www.javaspecialists.eu/archive/Issue098-References.html
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

	public StringNativeMapper() {
		super(String.class);
	}

	@Override
	public MemorySegment marshal(String str, NativeContext context) {
		final var allocator = context.allocator();
		return cache.computeIfAbsent(str, allocator::allocateFrom);
	}

	@Override
	public String unmarshal(MemorySegment address, Class<? extends String> type) {
		return unmarshal(address);
	}

	/**
	 * Helper - Unmarshals a string from the given address.
	 * @param address Memory address
	 * @return String at the given address or {@code null} for a {@link MemorySegment#NULL} address
	 */
	protected static String unmarshal(MemorySegment address) {
		return address.reinterpret(Integer.MAX_VALUE).getString(0);
	}
}
