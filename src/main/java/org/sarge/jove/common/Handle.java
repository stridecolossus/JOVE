package org.sarge.jove.common;

import java.lang.foreign.*;
import java.util.function.Function;

import org.sarge.jove.foreign.DefaultTransformer;

/**
 * A <i>handle</i> is an opaque, immutable wrapper for a native pointer.
 * @author Sarge
 */
public record Handle(MemorySegment address) {
	/**
	 * Constructor.
	 * @param address Memory address
	 * @throws NullPointerException if {@link #address} is {@link MemorySegment#NULL}
	 */
	public Handle {
		if(MemorySegment.NULL.equals(address)) throw new NullPointerException();
		address = address.asReadOnly();
	}

	/**
	 * Constructor given a literal address.
	 * @param address Memory address
	 */
	public Handle(long address) {
		this(MemorySegment.ofAddress(address));
	}

	/**
	 * @return Copy of the underlying memory address
	 */
	public MemorySegment address() {
		return MemorySegment.ofAddress(address.address());
	}

	/**
	 * Native transformer for a handle.
	 */
	public static class HandleTransformer extends DefaultTransformer<Handle> {
		@Override
		public MemorySegment marshal(Handle arg, SegmentAllocator allocator) {
			return arg.address;
		}

		@Override
		public Function<MemorySegment, Handle> unmarshal() {
			return Handle::new;
		}
	}
}
