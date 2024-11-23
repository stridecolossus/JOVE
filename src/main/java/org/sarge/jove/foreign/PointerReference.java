package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.ADDRESS;

import java.lang.foreign.MemorySegment;

import org.sarge.jove.common.Handle;

/**
 * A <i>pointer reference</i> maps to a native pointer-by-reference type.
 * @author Sarge
 */
public final class PointerReference {
	private MemorySegment address;

	/**
	 * @return This reference as an opaque handle
	 * @throws IllegalStateException if this pointer has not been initialised from the native layer
	 */
	public Handle handle() {
		if(address == null) throw new IllegalStateException("Pointer reference has not been initialised from the native layer");
   		final MemorySegment handle = address.get(ADDRESS, 0);
   		return new Handle(handle);
	}

	/**
	 * Native mapper for an pointer -by-reference value.
	 */
	public static final class PointerReferenceNativeMapper extends AbstractNativeMapper<PointerReference> {
		/**
		 * Constructor.
		 */
		public PointerReferenceNativeMapper() {
			super(PointerReference.class);
		}

		@Override
		public MemorySegment marshal(PointerReference ref, NativeContext context) {
			if(ref.address == null) {
				ref.address = context.allocator().allocate(ADDRESS);
			}
			return ref.address;
		}

		@Override
		public MemorySegment marshalNull(Class<? extends PointerReference> type) {
			throw new UnsupportedOperationException();
		}
	}
}
