package org.sarge.jove.util;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.*;

/**
 * Mock implementation that returns the same values on each invocation.
 * @author Sarge
 */
public class MockReferenceFactory implements ReferenceFactory {
	private final IntByReference integer = new IntByReference(1);
	private final PointerByReference ptr = new PointerByReference(new Pointer(2));

	@Override
	public IntByReference integer() {
		return integer;
	}

	@Override
	public PointerByReference pointer() {
		return ptr;
	}
}
