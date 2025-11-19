package org.sarge.jove.foreign;

import java.lang.foreign.*;

/**
 * An <i>integer reference</i> models a mutable, by-reference integer parameter.
 * @author Sarge
 */
public class IntegerReference extends NativeReference<Integer> {
	/**
	 * Constructor.
	 */
	public IntegerReference() {
		super(AddressLayout.ADDRESS);
	}

	@Override
	public Integer get() {
		final Integer value = super.get();
		if(value == null) {
			return 0;
		}
		else {
			return value;
		}
	}

	@Override
	protected Integer update(MemorySegment pointer, AddressLayout layout) {
		return pointer.get(ValueLayout.JAVA_INT, 0L);
	}
}
