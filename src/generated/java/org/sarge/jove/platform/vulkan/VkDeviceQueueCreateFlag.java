package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkDeviceQueueCreateFlag implements IntegerEnumeration {
 	VK_DEVICE_QUEUE_CREATE_PROTECTED_BIT(1), 	
 	VK_DEVICE_QUEUE_CREATE_FLAG_BITS_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkDeviceQueueCreateFlag(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
