package org.sarge.jove.common;

import java.util.Collection;

import org.sarge.jove.util.PointerToPointerArray;

import com.sun.jna.*;

/**
 * A <i>native object</i> is a resource created by the native layer referenced by a {@link Handle}.
 * @author Sarge
 */
public interface NativeObject {
	/**
	 * @return Handle
	 */
	Handle handle();

	private static Object pointer(NativeObject obj) {
		return Handle.CONVERTER.toNative(obj.handle(), null);
	}

	/**
	 * Helper - Converts the given objects to a JNA pointer-to-array-of-pointers.
	 * @param objects Native objects
	 * @return Pointer array
	 */
	static Memory array(Collection<? extends NativeObject> objects) {
		// Check for empty data
		if(objects.isEmpty()) {
			return null;
		}

		// Convert to array
		final Pointer[] pointers = objects
				.stream()
				.map(NativeObject::pointer)
				.toArray(Pointer[]::new);

		// Create pointer array
		return new PointerToPointerArray(pointers);
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
				// TODO - could we return handle here? i.e. does it cascade?
				return pointer(obj);
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
