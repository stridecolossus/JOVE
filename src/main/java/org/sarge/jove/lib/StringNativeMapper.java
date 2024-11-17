package org.sarge.jove.lib;

import java.lang.foreign.*;
import java.util.*;

/**
 * The <i>string native mapper</i> marshals a string as a native pointer to a null-terminated character array.
 * <p>
 * Note that since a Java string is an immutable type, the {@link #toNative(String, NativeContext)} method maintains a soft cache of marshalled strings.
 * <p>
 * @author Sarge
 */
public final class StringNativeMapper extends DefaultNativeMapper<String, MemorySegment> {
	private final Map<String, MemorySegment> cache = new WeakHashMap<>(); // TODO - soft cache

	public StringNativeMapper() {
		super(String.class, ValueLayout.ADDRESS);
	}

	@Override
	public MemorySegment toNative(String str, NativeContext context) {
		return cache.computeIfAbsent(str, __ -> context.allocator().allocateFrom(str));
	}

	@Override
	public String fromNative(MemorySegment address, Class<? extends String> type) {
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
