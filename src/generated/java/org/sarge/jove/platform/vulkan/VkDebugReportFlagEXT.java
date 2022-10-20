package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkDebugReportFlagEXT implements IntegerEnumeration {
 	VK_DEBUG_REPORT_INFORMATION_BIT_EXT(1), 	
 	VK_DEBUG_REPORT_WARNING_BIT_EXT(2), 	
 	VK_DEBUG_REPORT_PERFORMANCE_WARNING_BIT_EXT(4), 	
 	VK_DEBUG_REPORT_ERROR_BIT_EXT(8), 	
 	VK_DEBUG_REPORT_DEBUG_BIT_EXT(16), 	
 	VK_DEBUG_REPORT_FLAG_BITS_MAX_ENUM_EXT(2147483647); 	

	private final int value;
	
	private VkDebugReportFlagEXT(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
