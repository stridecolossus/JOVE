package org.sarge.jove.foreign;

import java.lang.foreign.*;

import org.sarge.jove.common.Handle;

/**
 * A <i>pointer</i> is an indirect reference to an off-heap address.
 * @author Sarge
 */
public class Pointer extends NativeReference<MemorySegment> {
	/**
	 * Default constructor for a simple pointer.
	 */
	public Pointer() {
		super(AddressLayout.ADDRESS);
	}

	/**
	 * Constructor for a memory block of the given size.
	 * @param size Block size (bytes)
	 */
	public Pointer(long size) {
		super(AddressLayout.ADDRESS.withTargetLayout(MemoryLayout.sequenceLayout(size, ValueLayout.JAVA_BYTE)));
	}

	/**
	 * Helper.
	 * Converts the off-heap address to a handle.
	 * @return Referenced handle or {@code null} if not updated
	 */
	public Handle handle() {
		final MemorySegment address = this.get();
		if((address == null) || MemorySegment.NULL.equals(address)) {
			return null;
		}
		else {
			return new Handle(address);
		}
	}

	@Override
	protected MemorySegment unmarshal(MemorySegment address, AddressLayout layout) {
		return address.get(layout, 0);
	}
}
