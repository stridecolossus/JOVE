package org.sarge.jove.common;

import static org.sarge.jove.util.Check.notNull;

import com.sun.jna.FromNativeContext;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ToNativeContext;
import com.sun.jna.TypeConverter;

/**
 * A <i>handle</i> is a wrapper for a JNA pointer.
 * @author Sarge
 */
public final class Handle {
	/**
	 * Native type converter for a handle.
	 */
	public static final TypeConverter CONVERTER = new TypeConverter() {
		@Override
		public Class<?> nativeType() {
			return Pointer.class;
		}

		@Override
		public Object toNative(Object value, ToNativeContext context) {
			if(value == null) {
				return null;
			}
			else {
				final Handle handle = (Handle) value;
				return handle.handle;
			}
		}

		@Override
		public Object fromNative(Object value, FromNativeContext context) {
			if(value == null) {
				return null;
			}
			else {
				return new Handle((Pointer) value);
			}
		}
	};

	/**
	 * TODO
	 * Builds a contiguous memory block from the given array of handles.
	 * @param handles
	 * @return
	 */
	public static Memory memory(Handle[] handles) {
		// Ignore empty arrays
		if(handles.length == 0) {
			return null;
		}

		// Build contiguous block of pointers
		final Memory mem = new Memory(Native.POINTER_SIZE * handles.length);
		for(int n = 0; n < handles.length; ++n) {
			if(handles[n] != null) {
				mem.setPointer(Native.POINTER_SIZE * n, handles[n].handle);
			}
		}

		return mem;
	}

	private final Pointer handle;

	/**
	 * Constructor.
	 * @param handle Pointer handle
	 */
	public Handle(Pointer handle) {
		this.handle = notNull(handle);
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof Handle that) && this.handle.equals(that.handle);
	}

	@Override
	public String toString() {
		return handle.toString();
	}
}
