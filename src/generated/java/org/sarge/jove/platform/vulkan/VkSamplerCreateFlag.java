package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkSamplerCreateFlag implements IntegerEnumeration {
 	VK_SAMPLER_CREATE_SUBSAMPLED_BIT_EXT(1), 	
 	VK_SAMPLER_CREATE_SUBSAMPLED_COARSE_RECONSTRUCTION_BIT_EXT(2), 	
 	VK_SAMPLER_CREATE_FLAG_BITS_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkSamplerCreateFlag(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
