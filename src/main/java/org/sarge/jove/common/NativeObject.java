package org.sarge.jove.common;

import java.util.Collection;

import org.sarge.jove.util.Check;
import org.sarge.jove.util.PointerArray;

import com.sun.jna.FromNativeContext;
import com.sun.jna.Pointer;
import com.sun.jna.ToNativeContext;
import com.sun.jna.TypeConverter;

/**
 * A <i>native object</i> is a resource managed by a native library and referenced by a JNA pointer.
 * @author Sarge
 */
public interface NativeObject {
	/**
	 * @return Handle
	 */
	Handle handle();

	/**
	 * A <i>transient native object</i> can be destroyed by the application.
	 */
	interface TransientNativeObject extends NativeObject {
		/**
		 * Destroys this object.
		 */
		void destroy();
	}

	/**
	 * A <i>handle</i> is an opaque wrapper for a JNA pointer referencing a native object.
	 */
	final class Handle {
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
		 * Creates an array of handle pointers for the given native objects.
		 * @param objects Native objects
		 * @return Pointer array
		 */
		public static Pointer[] toArray(Collection<? extends NativeObject> objects) {
			return objects
					.stream()
					.map(NativeObject::handle)
					.map(obj -> obj.handle)
					//.map(Pointer::nativeValue) // TODO - clone?
					.toArray(Pointer[]::new);
		}

		/**
		 * Creates a pointer array for the pointers to the given native objects.
		 * @param objects Native objects
		 * @return Pointer array or {@code null} if empty
		 */
		public static PointerArray toPointerArray(Collection<? extends NativeObject> objects) {
			if(objects.isEmpty()) {
				return null;
			}
			else {
				return new PointerArray(toArray(objects));
			}
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

		@Override
		public boolean equals(Object obj) {
			return (obj instanceof Handle that) && this.handle.equals(that.handle);
		}

		@Override
		public String toString() {
			return handle.toString();
		}
	}
}
