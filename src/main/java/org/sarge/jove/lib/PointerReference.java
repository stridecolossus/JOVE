package org.sarge.jove.lib;

import static java.lang.foreign.ValueLayout.ADDRESS;

import java.lang.foreign.MemorySegment;

/**
 * A <i>pointer reference</i> maps to a native pointer-by-reference type.
 * @author Sarge
 */
public final class PointerReference {
	private final Pointer pointer = new Pointer();

	/**
	 * @return This reference as an opaque handle
	 * @throws IllegalStateException if this pointer has not been initialised from the native layer
	 */
	public Handle handle() {
		if(!pointer.isAllocated()) throw new IllegalStateException("Pointer reference has not been initialised from the native layer");
   		final MemorySegment handle = pointer.address().get(ADDRESS, 0);
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
			super(PointerReference.class, ADDRESS);
		}

		@Override
		public MemorySegment toNative(PointerReference ref, NativeContext context) {
			return ref.pointer.allocate(ADDRESS, context);
		}

		@Override
		public MemorySegment toNativeNull(Class<? extends PointerReference> type) {
			throw new UnsupportedOperationException();
		}
	}
}
