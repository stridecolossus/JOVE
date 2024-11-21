package org.sarge.jove.lib;

import java.lang.foreign.MemorySegment;

import org.sarge.jove.lib.NativeMapper.ReturnMapper;

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

	/**
	 * Native mapper for a handle.
	 */
	public static final class HandleNativeMapper extends AbstractNativeMapper<Handle> implements ReturnMapper<Handle, MemorySegment> {
		/**
		 * Constructor.
		 */
		public HandleNativeMapper() {
			super(Handle.class);
		}

		@Override
		public MemorySegment marshal(Handle handle, NativeContext context) {
			return handle.address;
		}

		@Override
		public Handle unmarshal(MemorySegment address, Class<? extends Handle> type) {
			return new Handle(address);
		}

//		@Override
//		public Handle unmarshal(MemorySegment address, Class<? extends Handle> type) {
//			return new Handle(address);
//		}
	}
}
