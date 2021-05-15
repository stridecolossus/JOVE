package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkCompareOp implements IntegerEnumeration {
 	NEVER(0),
 	LESS(1),
 	EQUAL(2),
 	LESS_OR_EQUAL(3),
 	GREATER(4),
 	NOT_EQUAL(5),
 	GREATER_OR_EQUAL(6),
 	ALWAYS(7);

	private final int value;

	private VkCompareOp(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
