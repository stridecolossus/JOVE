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
	protected Integer unmarshal(MemorySegment address, AddressLayout layout) {
		return address.get(ValueLayout.JAVA_INT, 0L);
	}
}
