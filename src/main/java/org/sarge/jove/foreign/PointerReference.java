package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.ADDRESS;

import java.lang.foreign.*;
import java.util.function.BiConsumer;

import org.sarge.jove.common.Handle;

/**
 * A <i>pointer reference</i> maps to a native pointer-by-reference type.
 * @author Sarge
 */
public final class PointerReference {
	private MemorySegment address;
	private Handle handle;

	/**
	 * @return This reference as an opaque handle
	 * @throws IllegalStateException if this reference has not been initialised
	 */
	public Handle handle() {
		if(handle == null) throw new IllegalStateException("Pointer has not been initialised");
		return handle;
	}

	protected void set(MemorySegment ptr) {
		handle = new Handle(ptr);
	}

	private MemorySegment allocate(SegmentAllocator allocator) {
		if(address == null) {
			address = allocator.allocate(ADDRESS);
		}
		return address;
	}

	/**
	 * Native mapper for an pointer -by-reference value.
	 */
	public static class PointerReferenceTransform extends AbstractNativeTransformer<PointerReference, MemorySegment> {
		@Override
		public Class<PointerReference> type() {
			return PointerReference.class;
		}

		@Override
		public MemorySegment transform(PointerReference ref, SegmentAllocator allocator) {
			return ref.allocate(allocator);
		}

		@Override
		public MemorySegment empty() {
			throw new NullPointerException("A pointer reference cannot be null");
		}

		@Override
		public BiConsumer<MemorySegment, PointerReference> update() {
			return (address, ref) -> {
				assert address == ref.address;
				final MemorySegment ptr = address.get(ADDRESS, 0);
				ref.set(ptr);
			};
		}
	}
}
