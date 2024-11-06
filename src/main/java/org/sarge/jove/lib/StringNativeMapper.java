package org.sarge.jove.lib;

import java.lang.foreign.*;

/**
 * The <i>string native mapper</i> marshals a Java string as a native pointer to a null-terminated character array.
 * @author Sarge
 */
public class StringNativeMapper extends DefaultNativeMapper implements NativeTypeConverter<String, MemorySegment> {
	/**
	 * Constructor.
	 */
	public StringNativeMapper() {
		super(String.class, ValueLayout.ADDRESS);
	}

	@Override
	public MemorySegment toNative(String string, Class<?> __, Arena arena) {
		if(string == null) {
			return MemorySegment.NULL;
		}
		else {
			return arena.allocateFrom(string);
		}
	}

	@Override
	public String fromNative(MemorySegment address, Class<?> __) {
		if((address == null) || MemorySegment.NULL.equals(address)) {
			return null;
		}
		else {
    		return address.reinterpret(Integer.MAX_VALUE).getString(0);
		}
	}
}
