package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkMemoryAllocateFlag implements IntegerEnumeration {
 	VK_MEMORY_ALLOCATE_DEVICE_MASK_BIT(1), 	
 	VK_MEMORY_ALLOCATE_DEVICE_MASK_BIT_KHR(1), 	
 	VK_MEMORY_ALLOCATE_FLAG_BITS_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkMemoryAllocateFlag(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
