package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkSamplerReductionModeEXT implements IntegerEnumeration {
 	VK_SAMPLER_REDUCTION_MODE_WEIGHTED_AVERAGE_EXT(0), 	
 	VK_SAMPLER_REDUCTION_MODE_MIN_EXT(1), 	
 	VK_SAMPLER_REDUCTION_MODE_MAX_EXT(2), 	
 	VK_SAMPLER_REDUCTION_MODE_BEGIN_RANGE_EXT(0), 	
 	VK_SAMPLER_REDUCTION_MODE_END_RANGE_EXT(2), 	
 	VK_SAMPLER_REDUCTION_MODE_RANGE_SIZE_EXT(3), 	
 	VK_SAMPLER_REDUCTION_MODE_MAX_ENUM_EXT(2147483647); 	

	private final int value;
	
	private VkSamplerReductionModeEXT(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
