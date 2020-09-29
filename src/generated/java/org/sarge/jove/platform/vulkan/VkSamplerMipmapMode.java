package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkSamplerMipmapMode implements IntegerEnumeration {
 	VK_SAMPLER_MIPMAP_MODE_NEAREST(0), 	
 	VK_SAMPLER_MIPMAP_MODE_LINEAR(1), 	
 	VK_SAMPLER_MIPMAP_MODE_BEGIN_RANGE(0), 	
 	VK_SAMPLER_MIPMAP_MODE_END_RANGE(1), 	
 	VK_SAMPLER_MIPMAP_MODE_RANGE_SIZE(2), 	
 	VK_SAMPLER_MIPMAP_MODE_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkSamplerMipmapMode(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
