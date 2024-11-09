package org.sarge.jove.lib;

import static java.lang.foreign.MemorySegment.NULL;

import java.lang.foreign.MemorySegment;

import org.sarge.jove.lib.NativeMapper.ReturnMapper;

/**
 * A <i>handle</i> is an opaque wrapper for a native pointer.
 * @author Sarge
 */
public final class Handle extends Address {
	/**
	 * Constructor.
	 * @param address Memory address
	 */
	Handle(MemorySegment address) {
		super(address.asReadOnly());
	}

	/**
	 * Constructor given a literal address.
	 * @param address Memory address
	 */
	public Handle(long address) {
		super(MemorySegment.ofAddress(address));
	}

	@Override
	public String toString() {
		return String.format("Handle[%d]", address());
	}

	/**
	 * Native mapper for a handle.
	 */
	public static final class HandleNativeMapper extends AddressNativeMapper<Handle> implements ReturnMapper<MemorySegment> {
		/**
		 * Constructor.
		 */
		public HandleNativeMapper() {
			super(Handle.class);
		}

		@Override
		public Object toNativeNull(Class<?> type) {
			return NULL;
		}

		@Override
		public Handle fromNative(MemorySegment address, Class<?> type) {
			if(NULL.equals(address)) throw new NullPointerException();
			return new Handle(address);
		}
	}
}
