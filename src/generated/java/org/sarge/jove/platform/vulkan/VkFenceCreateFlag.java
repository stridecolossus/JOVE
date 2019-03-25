package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkFenceCreateFlag implements IntegerEnumeration {
 	VK_FENCE_CREATE_SIGNALED_BIT(1), 	
 	VK_FENCE_CREATE_FLAG_BITS_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkFenceCreateFlag(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}