package org.sarge.jove.common;

import com.sun.jna.*;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>handle</i> is an opaque wrapper for a JNA pointer.
 * @author Sarge
 */
public final class Handle {
	/**
	 * Creates a handle from the given reference returned by the native layer.
	 * @param ref Reference
	 * @return New handle
	 */
	public static Handle of(PointerByReference ref) {
		return new Handle(ref.getValue());
	}

	private final Pointer ptr;

	/**
	 * Constructor.
	 * @param ptr Native pointer
	 */
	public Handle(Pointer ptr) {
		this(Pointer.nativeValue(ptr));
	}

	/**
	 * Constructor.
	 * @param peer Underlying peer
	 */
	public Handle(long peer) {
		this.ptr = new Pointer(peer);
	}

	/**
	 * @return Underlying pointer
	 */
	Pointer pointer() {
		return ptr;
	}

	@Override
	public int hashCode() {
		return ptr.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof Handle that) &&
				this.ptr.equals(that.ptr);
	}

	@Override
	public String toString() {
		return ptr.toString();
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
				return handle.ptr;
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
}
