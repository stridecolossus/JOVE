package org.sarge.jove.lib;

import static java.lang.foreign.ValueLayout.ADDRESS;

import java.lang.foreign.MemorySegment;

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

	///////////

//	public static final class HandleArrayNativeMapper extends DefaultNativeMapper<Handle[]> {
//
//		public static MemorySegment ref;
//
//		public HandleArrayNativeMapper() {
//			super(Handle[].class, ADDRESS);
//		}
//
//		@Override
//		public Object toNative(Handle[] value, SegmentAllocator allocator) {
//			final MemorySegment root = allocator.allocate(ADDRESS, value.length);
//System.out.println("root="+root);
//			for(int n = 0; n < value.length; ++n) {
//				final MemorySegment e = allocator.allocate(ADDRESS);
//System.out.println("before="+e);
//				root.setAtIndex(ADDRESS, n, e);
//				value[n] = new Handle(e);
//			}
//			ref = root;
//			return root;
//		}
//	}

	///////////

	/**
	 * Native mapper for a handle.
	 */
	public static final class HandleNativeMapper extends DefaultNativeMapper<Handle, MemorySegment> {
		/**
		 * Constructor.
		 */
		public HandleNativeMapper() {
			super(Handle.class, ADDRESS);
		}

		@Override
		public MemorySegment toNative(Handle handle, NativeContext __) {
			return handle.address;
		}

		@Override
		public Handle fromNative(MemorySegment address, Class<? extends Handle> __) {
			return new Handle(address);
		}
	}
}
