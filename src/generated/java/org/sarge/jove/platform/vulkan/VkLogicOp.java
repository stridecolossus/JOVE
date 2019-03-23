package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkLogicOp implements IntegerEnumeration {
 	VK_LOGIC_OP_CLEAR(0), 	
 	VK_LOGIC_OP_AND(1), 	
 	VK_LOGIC_OP_AND_REVERSE(2), 	
 	VK_LOGIC_OP_COPY(3), 	
 	VK_LOGIC_OP_AND_INVERTED(4), 	
 	VK_LOGIC_OP_NO_OP(5), 	
 	VK_LOGIC_OP_XOR(6), 	
 	VK_LOGIC_OP_OR(7), 	
 	VK_LOGIC_OP_NOR(8), 	
 	VK_LOGIC_OP_EQUIVALENT(9), 	
 	VK_LOGIC_OP_INVERT(10), 	
 	VK_LOGIC_OP_OR_REVERSE(11), 	
 	VK_LOGIC_OP_COPY_INVERTED(12), 	
 	VK_LOGIC_OP_OR_INVERTED(13), 	
 	VK_LOGIC_OP_NAND(14), 	
 	VK_LOGIC_OP_SET(15), 	
 	VK_LOGIC_OP_BEGIN_RANGE(0), 	
 	VK_LOGIC_OP_END_RANGE(15), 	
 	VK_LOGIC_OP_RANGE_SIZE(16), 	
 	VK_LOGIC_OP_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkLogicOp(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
