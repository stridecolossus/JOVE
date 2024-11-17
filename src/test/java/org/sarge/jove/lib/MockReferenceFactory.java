package org.sarge.jove.lib;

public class MockReferenceFactory extends ReferenceFactory {
	public IntegerReference integer = new IntegerReference();
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
