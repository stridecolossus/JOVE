package org.sarge.jove.common;

import java.lang.foreign.*;

import org.sarge.jove.foreign.*;

/**
 * A <i>handle</i> is an opaque, immutable wrapper for a native pointer.
 * @author Sarge
 */
public final class Handle {
	private final MemorySegment address;

	/**
	 * Constructor.
	 * @param address Memory address
	 * @throws NullPointerException if {@link #address} is {@link MemorySegment#NULL}
	 */
	public Handle(MemorySegment address) {
		if(MemorySegment.NULL.equals(address)) throw new NullPointerException();
		this.address = address.asReadOnly();
	}

	/**
	 * Constructor given a literal address.
	 * @param address Memory address
	 */
	public Handle(long address) {
		this(MemorySegment.ofAddress(address));
	}

	/**
	 * @return Underlying memory address
	 */
	public MemorySegment address() {
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
		return String.format("Handle[%s]", address);
	}

	/**
	 * Creates a by-reference handle.
	 * @return Handle returned by-reference
	 */
	public static NativeReference<Handle> reference() {
		return new NativeReference<>() {
    		@Override
    		protected Handle update(MemorySegment address) {
    			final MemorySegment handle = address.get(ValueLayout.ADDRESS, 0L);
    			return new Handle(handle);
    		}
    	};
	}

	/**
	 * Native transformer for a handle.
	 */
	public static class HandleTransformer implements AddressTransformer<Handle, MemorySegment> {
		@Override
		public Object marshal(Handle arg, SegmentAllocator allocator) {
			return arg.address;
		}

		@Override
		public Handle unmarshal(MemorySegment address) {
			return new Handle(address);
		}
	}
}
