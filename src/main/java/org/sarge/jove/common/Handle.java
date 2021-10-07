package org.sarge.jove.common;

import com.sun.jna.FromNativeContext;
import com.sun.jna.Pointer;
import com.sun.jna.ToNativeContext;
import com.sun.jna.TypeConverter;

/**
 * A <i>handle</i> is an opaque wrapper for a JNA pointer.
 * @author Sarge
 */
public final class Handle {
	private final Pointer ptr;

	/**
	 * Constructor.
	 * @param handle Pointer handle
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
	 * @return Copy of the underlying pointer
	 */
	public Pointer toPointer() {
		return new Pointer(Pointer.nativeValue(ptr));
	}

	@Override
	public int hashCode() {
		return ptr.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return (obj == this) || ((obj instanceof Handle that) && this.ptr.equals(that.ptr));
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
			if(value instanceof Handle handle) {
				return handle.ptr;
			}
			else {
				return null;
			}
		}

		@Override
		public Object fromNative(Object value, FromNativeContext context) {
			if(value instanceof Pointer ptr) {
				return new Handle(ptr);
			}
			else {
				return null;
			}
		}
	};
}
