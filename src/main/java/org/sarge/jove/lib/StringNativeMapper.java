package org.sarge.jove.lib;

import static java.lang.foreign.MemorySegment.NULL;

import java.lang.foreign.*;

import org.sarge.jove.lib.NativeMapper.ReturnMapper;

/**
 * The <i>string native mapper</i> marshals a string as a native pointer to a null-terminated character array.
 * @author Sarge
 */
public final class StringNativeMapper extends AbstractNativeMapper<String> implements ReturnMapper<MemorySegment> {
	/**
	 * Constructor.
	 */
	public StringNativeMapper() {
		super(String.class, ValueLayout.ADDRESS);
	}

	@Override
	public MemorySegment toNative(String string, Arena arena) {
		return arena.allocateFrom(string);
	}

	@Override
	public Object toNativeNull(Class<?> type) {
		return NULL;
	}

	@Override
	public String fromNative(MemorySegment address, Class<?> type) {
		return fromNative(address);
	}

	/**
	 * Helper - Marshals a string from the given address.
	 * @param address Memory address
	 * @return String at the given address or {@code null} for a {@link MemorySegment#NULL} address
	 */
	protected static String fromNative(MemorySegment address) {
		if(NULL.equals(address)) {
			return null;
		}
		else {
    		return address.reinterpret(Integer.MAX_VALUE).getString(0);
		}
	}
}
