package org.sarge.jove.foreign;

import org.sarge.jove.foreign.*;

public class MockReferenceFactory extends ReferenceFactory {
	public IntegerReference integer = new IntegerReference() {
		@Override
		public int value() {
			return 1;
		}
	};

	public PointerReference pointer = new PointerReference();

	@Override
	public IntegerReference integer() {
		return integer;
	}

	@Override
	public PointerReference pointer() {
		return pointer;
	}
}
