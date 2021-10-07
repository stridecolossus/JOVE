package org.sarge.jove.common;

import java.util.Collection;

import com.sun.jna.FromNativeContext;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ToNativeContext;
import com.sun.jna.TypeConverter;

/**
 * A <i>native object</i> is a resource created by the native layer referenced by a JNA pointer.
 * @author Sarge
 */
public interface NativeObject {
	/**
	 * @return Handle
	 */
	Handle handle();

	/**
	 * Converts the given objects to an array of handles.
	 * @param objects Native objects
	 * @return Handle array
	 */
	public static Memory toArray(Collection<? extends NativeObject> objects) {
		// Check for empty data
		if(objects.isEmpty()) {
			return null;
		}

		// Convert to array
		final Pointer[] array = objects
				.stream()
				.map(NativeObject::handle)
				.map(Handle::toPointer)
				.toArray(Pointer[]::new);

		// Create contiguous memory block
		final Memory mem = new Memory(Native.POINTER_SIZE * array.length);
		for(int n = 0; n < array.length; ++n) {
			mem.setPointer(n * Native.POINTER_SIZE, array[n]);
		}

		return mem;
	}

	/**
	 * JNA type converter for a native object.
	 */
	TypeConverter CONVERTER = new TypeConverter() {
		@Override
		public Class<?> nativeType() {
			return Pointer.class;
		}

		@Override
		public Object toNative(Object value, ToNativeContext context) {
			if(value instanceof NativeObject obj) {
				return obj.handle().toPointer();
			}
			else {
				return null;
			}
		}

		@Override
		public Object fromNative(Object nativeValue, FromNativeContext context) {
			throw new UnsupportedOperationException();
		}
	};
}
