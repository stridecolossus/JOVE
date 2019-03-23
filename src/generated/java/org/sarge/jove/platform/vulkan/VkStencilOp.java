package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkStencilOp implements IntegerEnumeration {
 	VK_STENCIL_OP_KEEP(0), 	
 	VK_STENCIL_OP_ZERO(1), 	
 	VK_STENCIL_OP_REPLACE(2), 	
 	VK_STENCIL_OP_INCREMENT_AND_CLAMP(3), 	
 	VK_STENCIL_OP_DECREMENT_AND_CLAMP(4), 	
 	VK_STENCIL_OP_INVERT(5), 	
 	VK_STENCIL_OP_INCREMENT_AND_WRAP(6), 	
 	VK_STENCIL_OP_DECREMENT_AND_WRAP(7), 	
 	VK_STENCIL_OP_BEGIN_RANGE(0), 	
 	VK_STENCIL_OP_END_RANGE(7), 	
 	VK_STENCIL_OP_RANGE_SIZE(8), 	
 	VK_STENCIL_OP_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkStencilOp(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
