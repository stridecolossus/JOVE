package org.sarge.jove.common;

import java.util.Collection;

import org.sarge.jove.util.NativeHelper.PointerToPointerArray;

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
				.map(NativeObject::handle)
				.map(Handle::pointer)
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
			if(value == null) {
				return null;
			}
			else {
				final var obj = (NativeObject) value;
				return obj.handle().pointer();
			}
		}

		@Override
		public Object fromNative(Object nativeValue, FromNativeContext context) {
			throw new UnsupportedOperationException();
		}
	};
}
