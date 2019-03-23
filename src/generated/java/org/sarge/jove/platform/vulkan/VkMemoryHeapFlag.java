package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkMemoryHeapFlag implements IntegerEnumeration {
 	VK_MEMORY_HEAP_DEVICE_LOCAL_BIT(1), 	
 	VK_MEMORY_HEAP_MULTI_INSTANCE_BIT(2), 	
 	VK_MEMORY_HEAP_MULTI_INSTANCE_BIT_KHR(2), 	
 	VK_MEMORY_HEAP_FLAG_BITS_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkMemoryHeapFlag(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
