package org.sarge.jove.common;

import java.lang.foreign.*;
import java.util.function.*;

import org.sarge.jove.foreign.ReferenceTransformer;

/**
 * A <i>handle</i> is an opaque, immutable wrapper for a native pointer.
 * @author Sarge
 */
public final class Handle {
	private final MemorySegment address;

	/**
	 * Constructor.
	 * @param address Memory address
	 */
	public Handle(MemorySegment address) {
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
		return String.format("Handle[%d]", address.address());
	}

	// TODO
	public <T> T[] array(int length, MemoryLayout layout, IntFunction<T[]> factory, Function<MemorySegment, T> mapper) {
		// Resize handle to array layout
		final long size = layout.byteSize();
		final MemorySegment segment = address.reinterpret(size * length);

		// Allocate array
		final T[] array = factory.apply(length);

		// Extract elements
		for(int n = 0; n < length; ++n) {
			final MemorySegment e = segment.getAtIndex(ValueLayout.ADDRESS, n);
			array[n] = mapper.apply(e);
		}

		return array;
	}

	/**
	 * Native transformer for a handle.
	 */
	public static class HandleTransformer implements ReferenceTransformer<Handle, MemorySegment> {
		@Override
		public Object marshal(Handle arg, SegmentAllocator allocator) {
			return arg.address;
		}

		public MemorySegment marshal(Handle handle) {
			return handle.address;
		}

		@Override
		public Handle unmarshal(MemorySegment address) {
			return new Handle(address);
		}
	}
}
