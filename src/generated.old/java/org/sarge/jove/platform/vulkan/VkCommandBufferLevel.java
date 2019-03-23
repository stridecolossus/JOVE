package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkCommandBufferLevel implements IntegerEnumeration {
 	VK_COMMAND_BUFFER_LEVEL_PRIMARY(0), 	
 	VK_COMMAND_BUFFER_LEVEL_SECONDARY(1), 	
 	VK_COMMAND_BUFFER_LEVEL_BEGIN_RANGE(0), 	
 	VK_COMMAND_BUFFER_LEVEL_END_RANGE(1), 	
 	VK_COMMAND_BUFFER_LEVEL_RANGE_SIZE(2), 	
 	VK_COMMAND_BUFFER_LEVEL_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkCommandBufferLevel(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
