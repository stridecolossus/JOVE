package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkSamplerAddressMode implements IntegerEnumeration {
 	VK_SAMPLER_ADDRESS_MODE_REPEAT(0), 	
 	VK_SAMPLER_ADDRESS_MODE_MIRRORED_REPEAT(1), 	
 	VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE(2), 	
 	VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_BORDER(3), 	
 	VK_SAMPLER_ADDRESS_MODE_MIRROR_CLAMP_TO_EDGE(4), 	
 	VK_SAMPLER_ADDRESS_MODE_BEGIN_RANGE(0), 	
 	VK_SAMPLER_ADDRESS_MODE_END_RANGE(3), 	
 	VK_SAMPLER_ADDRESS_MODE_RANGE_SIZE(4), 	
 	VK_SAMPLER_ADDRESS_MODE_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkSamplerAddressMode(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
