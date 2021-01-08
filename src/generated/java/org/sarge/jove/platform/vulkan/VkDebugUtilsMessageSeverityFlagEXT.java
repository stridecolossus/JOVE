package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkDebugUtilsMessageSeverityFlagEXT implements IntegerEnumeration {
 	VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT(1),
 	VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT(16),
 	VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT(256),
 	VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT(4096);
	private final int value;

	private VkDebugUtilsMessageSeverityFlagEXT(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
