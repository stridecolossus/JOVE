package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkCommandPoolCreateFlag implements IntegerEnumeration {
 	VK_COMMAND_POOL_CREATE_TRANSIENT_BIT(1), 	
 	VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT(2), 	
 	VK_COMMAND_POOL_CREATE_PROTECTED_BIT(4), 	
 	VK_COMMAND_POOL_CREATE_FLAG_BITS_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkCommandPoolCreateFlag(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
