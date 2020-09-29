package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkCullModeFlag implements IntegerEnumeration {
 	VK_CULL_MODE_NONE(0), 	
 	VK_CULL_MODE_FRONT_BIT(1), 	
 	VK_CULL_MODE_BACK_BIT(2), 	
 	VK_CULL_MODE_FRONT_AND_BACK(3), 	
 	VK_CULL_MODE_FLAG_BITS_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkCullModeFlag(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
