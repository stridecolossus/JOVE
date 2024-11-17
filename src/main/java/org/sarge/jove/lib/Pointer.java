package org.sarge.jove.lib;

import java.lang.foreign.*;
import java.util.Objects;

/**
 * A <i>pointer</i> is a mutable native address.
 * TODO
 * @author Sarge
 */
public final class Pointer {
	private MemorySegment address;

	/**
	 * @return Whether this pointer has been allocated
	 */
	public final boolean isAllocated() {
		return Objects.nonNull(address);
	}

	/**
	 * @return Memory address
	 */
	public final MemorySegment address() {
		return address;
	}

	/**
	 * Allocates this pointer as required.
	 * @param layout		Memory layout
	 * @param context		Context
	 * @return Allocated address
	 */
	protected MemorySegment allocate(MemoryLayout layout, NativeContext context) {
		if(!isAllocated()) {
			address = context.allocator().allocate(layout);
		}
		return address;
	}

	@Override
	public int hashCode() {
		return Objects.hash(address);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof Pointer that) &&
				Objects.equals(this.address, that.address);
	}
}
