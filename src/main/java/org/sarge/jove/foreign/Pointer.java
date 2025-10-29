package org.sarge.jove.foreign;

import java.lang.foreign.*;

import org.sarge.jove.common.Handle;

/**
 * A <i>pointer</i> is an indirect reference to an off-heap address.
 * @author Sarge
 */
public class Pointer extends NativeReference<Handle> {
	@Override
	protected void update(MemorySegment pointer) {
		final MemorySegment address = pointer.get(ValueLayout.ADDRESS, 0L);
		if(MemorySegment.NULL.equals(address)) {
			set(null);
		}
		else {
			set(new Handle(address));
		}
	}
}
