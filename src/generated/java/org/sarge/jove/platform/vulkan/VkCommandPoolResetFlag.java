package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkCommandPoolResetFlag implements IntegerEnumeration {
 	VK_COMMAND_POOL_RESET_RELEASE_RESOURCES_BIT(1), 	
 	VK_COMMAND_POOL_RESET_FLAG_BITS_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkCommandPoolResetFlag(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
