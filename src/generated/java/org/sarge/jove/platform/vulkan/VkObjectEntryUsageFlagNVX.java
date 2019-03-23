package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkObjectEntryUsageFlagNVX implements IntegerEnumeration {
 	VK_OBJECT_ENTRY_USAGE_GRAPHICS_BIT_NVX(1), 	
 	VK_OBJECT_ENTRY_USAGE_COMPUTE_BIT_NVX(2), 	
 	VK_OBJECT_ENTRY_USAGE_FLAG_BITS_MAX_ENUM_NVX(2147483647); 	

	private final int value;
	
	private VkObjectEntryUsageFlagNVX(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
