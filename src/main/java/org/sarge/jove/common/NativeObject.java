package org.sarge.jove.common;

import java.util.Collection;

import org.sarge.jove.util.PointerArray;

import com.sun.jna.FromNativeContext;
import com.sun.jna.Memory;
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
	 * Helper - Converts the given objects to an array of handles.
	 * @param objects Native objects
	 * @return Handle array
	 */
	static Memory array(Collection<? extends NativeObject> objects) {
		// Check for empty data
		if(objects.isEmpty()) {
			return null;
		}

		// Convert to array
		final Pointer[] pointers = objects
				.stream()
				.map(NativeObject::handle)
				.map(Handle::toPointer)
				.toArray(Pointer[]::new);

		// Create pointer array
		return new PointerArray(pointers);
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
