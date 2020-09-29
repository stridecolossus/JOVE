package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkDebugUtilsMessageTypeFlagEXT implements IntegerEnumeration {
 	VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT(1), 	
 	VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT(2), 	
 	VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT(4), 	
 	VK_DEBUG_UTILS_MESSAGE_TYPE_FLAG_BITS_MAX_ENUM_EXT(2147483647); 	

	private final int value;
	
	private VkDebugUtilsMessageTypeFlagEXT(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
