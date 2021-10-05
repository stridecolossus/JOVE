package org.sarge.jove.common;

import static org.sarge.lib.util.Check.notNull;

import java.util.Arrays;
import java.util.Collection;

import com.sun.jna.FromNativeContext;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ToNativeContext;
import com.sun.jna.TypeConverter;

/**
 * A <i>handle</i> is an opaque wrapper for a JNA pointer.
 * @author Sarge
 */
public final class Handle {
	/**
	 * Converts the given objects to an array of handles.
	 * @param objects Native objects
	 * @return Handle array
	 */
	public static Handle toArray(Collection<? extends NativeObject> objects) {
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

		// Create handle
		return new Handle(new PointerArray(array));
	}

	private final Pointer handle;

	/**
	 * Constructor.
	 * @param handle Pointer handle
	 */
	public Handle(Pointer handle) {
		this.handle = notNull(handle);
	}

	// TODO
	public Handle(long peer) {
		this(new Pointer(peer));
	}

	/**
	 * @return Copy of the underlying pointer
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
		return (obj == this) || ((obj instanceof Handle that) && this.handle.equals(that.handle));
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
	 * Array wrapper.
	 */
	private static class PointerArray extends Memory {
		private final Pointer[] array;

		private PointerArray(Pointer[] array) {
			super(Native.POINTER_SIZE * array.length);
			for(int n = 0; n < array.length; ++n) {
				setPointer(n * Native.POINTER_SIZE, array[n]);
			}
			this.array = array;
		}

		@Override
		public boolean equals(Object obj) {
			return (obj == this) || (obj instanceof PointerArray that) && Arrays.equals(this.array, that.array);
		}
	}
}
