package org.sarge.jove.common;

import java.util.Arrays;
import java.util.Collection;

import org.sarge.lib.util.Check;

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
	 * A <i>handle</i> is an opaque wrapper for a JNA pointer.
	 */
	final class Handle {
		/**
		 * Converts the given objects to an array of handles.
		 * @param objects Native objects
		 * @return Handle array
		 */
		public static HandleArray toArray(Collection<? extends NativeObject> objects) {
			// Check for empty data
			if(objects.isEmpty()) {
				return null;
			}

			// Convert to array
			final Pointer[] array = objects
					.stream()
					.map(NativeObject::handle)
					.map(handle -> handle.handle)
					.toArray(Pointer[]::new);

			// Create JNA wrapper
			return new HandleArray(array);
		}

		private final Pointer handle;

		/**
		 * Constructor.
		 * @param handle Pointer handle
		 */
		public Handle(Pointer handle) {
			Check.notNull(handle);
			this.handle = new Pointer(Pointer.nativeValue(handle));
		}

		/**
		 * @return Copy of the underlying JNA pointer
		 */
		public Pointer toPointer() {
			return new Pointer(Pointer.nativeValue(handle));
		}

		@Override
		public int hashCode() {
			return handle.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return (obj instanceof Handle that) && this.handle.equals(that.handle);
		}

		@Override
		public String toString() {
			return handle.toString();
		}

		/**
		 * JNA type converter for this handle.
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
					// TODO - clone pointer?
					// TODO - could differentiate cases ~ context, e.g. clone for structure
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
		 * Array of handles.
		 */
		public static class HandleArray extends Memory {
			private final Pointer[] array;

			private HandleArray(Pointer[] array) {
				super(Native.POINTER_SIZE * array.length);
				for(int n = 0; n < array.length; ++n) {
					setPointer(n * Native.POINTER_SIZE, array[n]);
				}
				this.array = array;
			}

			@Override
			public boolean equals(Object obj) {
				return (obj instanceof HandleArray that) && Arrays.equals(array, that.array);
			}
		}
	}
}
