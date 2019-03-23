package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkBlendFactor implements IntegerEnumeration {
 	VK_BLEND_FACTOR_ZERO(0), 	
 	VK_BLEND_FACTOR_ONE(1), 	
 	VK_BLEND_FACTOR_SRC_COLOR(2), 	
 	VK_BLEND_FACTOR_ONE_MINUS_SRC_COLOR(3), 	
 	VK_BLEND_FACTOR_DST_COLOR(4), 	
 	VK_BLEND_FACTOR_ONE_MINUS_DST_COLOR(5), 	
 	VK_BLEND_FACTOR_SRC_ALPHA(6), 	
 	VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA(7), 	
 	VK_BLEND_FACTOR_DST_ALPHA(8), 	
 	VK_BLEND_FACTOR_ONE_MINUS_DST_ALPHA(9), 	
 	VK_BLEND_FACTOR_CONSTANT_COLOR(10), 	
 	VK_BLEND_FACTOR_ONE_MINUS_CONSTANT_COLOR(11), 	
 	VK_BLEND_FACTOR_CONSTANT_ALPHA(12), 	
 	VK_BLEND_FACTOR_ONE_MINUS_CONSTANT_ALPHA(13), 	
 	VK_BLEND_FACTOR_SRC_ALPHA_SATURATE(14), 	
 	VK_BLEND_FACTOR_SRC1_COLOR(15), 	
 	VK_BLEND_FACTOR_ONE_MINUS_SRC1_COLOR(16), 	
 	VK_BLEND_FACTOR_SRC1_ALPHA(17), 	
 	VK_BLEND_FACTOR_ONE_MINUS_SRC1_ALPHA(18), 	
 	VK_BLEND_FACTOR_BEGIN_RANGE(0), 	
 	VK_BLEND_FACTOR_END_RANGE(18), 	
 	VK_BLEND_FACTOR_RANGE_SIZE(19), 	
 	VK_BLEND_FACTOR_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkBlendFactor(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
