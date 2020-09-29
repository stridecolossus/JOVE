package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkComponentSwizzle implements IntegerEnumeration {
 	VK_COMPONENT_SWIZZLE_IDENTITY(0), 	
 	VK_COMPONENT_SWIZZLE_ZERO(1), 	
 	VK_COMPONENT_SWIZZLE_ONE(2), 	
 	VK_COMPONENT_SWIZZLE_R(3), 	
 	VK_COMPONENT_SWIZZLE_G(4), 	
 	VK_COMPONENT_SWIZZLE_B(5), 	
 	VK_COMPONENT_SWIZZLE_A(6), 	
 	VK_COMPONENT_SWIZZLE_BEGIN_RANGE(0), 	
 	VK_COMPONENT_SWIZZLE_END_RANGE(6), 	
 	VK_COMPONENT_SWIZZLE_RANGE_SIZE(7), 	
 	VK_COMPONENT_SWIZZLE_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkComponentSwizzle(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
