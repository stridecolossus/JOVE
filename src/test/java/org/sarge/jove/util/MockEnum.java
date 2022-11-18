package org.sarge.jove.util;

/**
 * Mock implementation.
 */
enum MockEnum implements IntegerEnumeration {
	A(1),
	B(2),
	C(4);

	private final int value;

	private MockEnum(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
