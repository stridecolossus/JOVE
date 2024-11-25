package org.sarge.jove.common;

import java.lang.foreign.*;
import java.util.function.*;

import org.sarge.jove.foreign.AbstractNativeMapper;

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
		return String.format("Handle[%s]", address);
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
	 * Native mapper for a handle.
	 */
	public static final class HandleNativeMapper extends AbstractNativeMapper<Handle, MemorySegment> {
		@Override
		public Class<Handle> type() {
			return Handle.class;
		}

		@Override
		public MemorySegment marshal(Handle handle, SegmentAllocator allocator) {
			return handle.address;
		}

		@Override
		public Function<MemorySegment, Handle> returns() {
			return Handle::new;
		}
	}
}
