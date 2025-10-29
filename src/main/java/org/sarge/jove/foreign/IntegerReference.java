package org.sarge.jove.foreign;

import java.lang.foreign.*;

/**
 * An <i>integer reference</i> is models a mutable, by-reference integer parameter.
 * @author Sarge
 */
public class IntegerReference extends NativeReference<Integer> {
	/**
	 * Constructor.
	 */
	public IntegerReference() {
		set(0);
	}

	@Override
	protected void update(MemorySegment pointer) {
		final int value = pointer.get(ValueLayout.JAVA_INT, 0L);
		set(value);
	}
}
