package org.sarge.jove.foreign;

import java.lang.foreign.*;

import org.sarge.jove.common.Handle;

/**
 * A <i>pointer</i> is an indirect reference to an off-heap address.
 * @author Sarge
 */
public class Pointer extends NativeReference<Handle> {
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

	@Override
	protected Handle update(MemorySegment pointer, AddressLayout layout) {
		final MemorySegment address = pointer.get(layout, 0L);
		if(MemorySegment.NULL.equals(address)) {
			return null;
		}
		return new Handle(address);
	}
}
