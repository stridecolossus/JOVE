package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkSamplerYcbcrRange implements IntegerEnumeration {
 	VK_SAMPLER_YCBCR_RANGE_ITU_FULL(0), 	
 	VK_SAMPLER_YCBCR_RANGE_ITU_NARROW(1), 	
 	VK_SAMPLER_YCBCR_RANGE_ITU_FULL_KHR(0), 	
 	VK_SAMPLER_YCBCR_RANGE_ITU_NARROW_KHR(1), 	
 	VK_SAMPLER_YCBCR_RANGE_BEGIN_RANGE(0), 	
 	VK_SAMPLER_YCBCR_RANGE_END_RANGE(1), 	
 	VK_SAMPLER_YCBCR_RANGE_RANGE_SIZE(2), 	
 	VK_SAMPLER_YCBCR_RANGE_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkSamplerYcbcrRange(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
