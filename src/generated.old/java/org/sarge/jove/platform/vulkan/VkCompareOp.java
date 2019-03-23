package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkCompareOp implements IntegerEnumeration {
 	VK_COMPARE_OP_NEVER(0), 	
 	VK_COMPARE_OP_LESS(1), 	
 	VK_COMPARE_OP_EQUAL(2), 	
 	VK_COMPARE_OP_LESS_OR_EQUAL(3), 	
 	VK_COMPARE_OP_GREATER(4), 	
 	VK_COMPARE_OP_NOT_EQUAL(5), 	
 	VK_COMPARE_OP_GREATER_OR_EQUAL(6), 	
 	VK_COMPARE_OP_ALWAYS(7), 	
 	VK_COMPARE_OP_BEGIN_RANGE(0), 	
 	VK_COMPARE_OP_END_RANGE(7), 	
 	VK_COMPARE_OP_RANGE_SIZE(8), 	
 	VK_COMPARE_OP_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkCompareOp(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
