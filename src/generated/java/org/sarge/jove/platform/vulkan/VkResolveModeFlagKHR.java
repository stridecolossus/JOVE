package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkResolveModeFlagKHR implements IntEnum {
 	VK_RESOLVE_MODE_NONE_KHR(0), 	
 	VK_RESOLVE_MODE_SAMPLE_ZERO_BIT_KHR(1), 	
 	VK_RESOLVE_MODE_AVERAGE_BIT_KHR(2), 	
 	VK_RESOLVE_MODE_MIN_BIT_KHR(4), 	
 	VK_RESOLVE_MODE_MAX_BIT_KHR(8), 	
 	VK_RESOLVE_MODE_FLAG_BITS_MAX_ENUM_KHR(2147483647); 	

	private final int value;
	
	private VkResolveModeFlagKHR(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
