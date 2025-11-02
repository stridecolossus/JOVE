package org.sarge.jove.common;

import java.lang.foreign.*;
import java.util.function.Function;

import org.sarge.jove.foreign.Transformer;

/**
 * A <i>handle</i> is an opaque, immutable wrapper for a native pointer.
 * @author Sarge
 */
public record Handle(MemorySegment address) {
	/**
	 * Constructor.
	 * @param address Memory address
	 * @throws NullPointerException if {@link #address} is {@code null} or {@link MemorySegment#NULL}
	 */
	public Handle {
		if(MemorySegment.NULL.equals(address)) {
			throw new NullPointerException();
		}
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
		// TODO - no need for copy? since address.asReadOnly() in ctor
		return MemorySegment.ofAddress(address.address());
	}

	/**
	 * Native transformer for a handle.
	 */
	public static class HandleTransformer implements Transformer<Handle, MemorySegment> {
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
