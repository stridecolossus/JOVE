package org.sarge.jove.lib;

import java.lang.foreign.*;

/**
 * A <i>handle</i> is an opaque wrapper for an arbitrary native pointer.
 * @author Sarge
 */
public final class Handle {
	private final MemorySegment address;

	public Handle(long address) {
		this(MemorySegment.ofAddress(address));
	}

	private Handle(MemorySegment address) {
		this.address = address;
	}

	MemorySegment address() {
		return MemorySegment.ofAddress(address.address());
	}

	@Override
	public int hashCode() {
		return address.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof Handle that) &&
				this.address.equals(that.address);
	}

	@Override
	public String toString() {
		return String.format("Handle[%d]", address.address());
	}

	/**
	 * Native mapper for a handle.
	 */
	public static class HandleNativeMapper extends DefaultNativeMapper implements NativeTypeConverter<Handle, MemorySegment> {
		/**
		 * Constructor.
		 */
		public HandleNativeMapper() {
			super(Handle.class, ValueLayout.ADDRESS);
		}

		@Override
		public MemorySegment toNative(Handle handle, Class<?> type, Arena arena) {
			if(handle == null) {
				return MemorySegment.NULL;
			}
			else {
				return handle.address;
			}
		}

		@Override
		public Handle fromNative(MemorySegment address, Class<?> __) {
			return new Handle(address);
		}
	}
}
