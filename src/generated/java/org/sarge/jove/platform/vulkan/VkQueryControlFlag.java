package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkQueryControlFlag implements IntegerEnumeration {
 	VK_QUERY_CONTROL_PRECISE_BIT(1), 	
 	VK_QUERY_CONTROL_FLAG_BITS_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkQueryControlFlag(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
