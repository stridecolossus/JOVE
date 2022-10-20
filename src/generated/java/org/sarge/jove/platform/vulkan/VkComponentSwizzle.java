package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkComponentSwizzle implements IntegerEnumeration {
 	IDENTITY(0),
 	ZERO(1),
 	ONE(2),
 	R(3),
 	G(4),
 	B(5),
 	A(6);

	private final int value;

	private VkComponentSwizzle(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
