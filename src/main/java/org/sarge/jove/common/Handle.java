package org.sarge.jove.common;

import java.util.Collection;
import java.util.function.Function;

import org.sarge.jove.util.Check;
import org.sarge.jove.util.PointerArray;

import com.sun.jna.FromNativeContext;
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

	public static <T> Pointer[] toArray(Collection<T> handles, Function<T, Handle> mapper) {
		return handles.stream().map(mapper).map(handle -> handle.handle).toArray(Pointer[]::new);
	}

	/**
	 * Helper - Creates a pointer-array from the given collection.
	 * @param handles Handles
	 * @return Pointer-array
	 */
	public static PointerArray toPointerArray(Collection<Handle> handles) {
		final var array = handles.stream().map(handle -> handle.handle).toArray(Pointer[]::new);
		return new PointerArray(array);
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
	 * Helper - Creates a pointer-array from this handle.
	 * @return New pointer-array
	 */
	public PointerArray toPointerArray() {
		return new PointerArray(new Pointer[]{handle});
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
