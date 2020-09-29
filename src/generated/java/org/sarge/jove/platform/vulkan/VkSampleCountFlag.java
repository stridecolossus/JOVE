package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkSampleCountFlag implements IntegerEnumeration {
 	VK_SAMPLE_COUNT_1_BIT(1), 	
 	VK_SAMPLE_COUNT_2_BIT(2), 	
 	VK_SAMPLE_COUNT_4_BIT(4), 	
 	VK_SAMPLE_COUNT_8_BIT(8), 	
 	VK_SAMPLE_COUNT_16_BIT(16), 	
 	VK_SAMPLE_COUNT_32_BIT(32), 	
 	VK_SAMPLE_COUNT_64_BIT(64), 	
 	VK_SAMPLE_COUNT_FLAG_BITS_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkSampleCountFlag(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
