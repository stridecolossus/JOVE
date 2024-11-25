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
	 */
	public Handle handle() {
		return handle;
	}

	protected void set(MemorySegment ptr) {
		handle = new Handle(ptr);
	}

	/**
	 * Native mapper for an pointer -by-reference value.
	 */
	public static class PointerReferenceMapper extends AbstractNativeMapper<PointerReference, MemorySegment> {
		@Override
		public Class<PointerReference> type() {
			return PointerReference.class;
		}

		@Override
		public MemorySegment marshal(PointerReference ref, SegmentAllocator allocator) {
			if(ref.address == null) {
				ref.address = allocator.allocate(ADDRESS);
			}
			return ref.address;
		}

		@Override
		public MemorySegment marshalNull() {
			throw new NullPointerException();
		}

		@Override
		public BiConsumer<MemorySegment, PointerReference> reference() {
			return (address, ref) -> {
				assert address == ref.address;
				final MemorySegment ptr = address.get(ADDRESS, 0);
				ref.set(ptr);
			};
		}
	}
}
