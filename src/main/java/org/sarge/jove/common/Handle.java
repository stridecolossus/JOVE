package org.sarge.jove.common;

import java.lang.foreign.*;
import java.util.function.Function;

import org.sarge.jove.foreign.Transformer;

/**
 * A <i>handle</i> is an immutable wrapper for a native memory address.
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

	@Override
	public final String toString() {
		return "0x" + Long.toHexString(address.address());
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
