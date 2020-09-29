package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkMemoryPropertyFlag implements IntegerEnumeration {
 	VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT(1), 	
 	VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT(2), 	
 	VK_MEMORY_PROPERTY_HOST_COHERENT_BIT(4), 	
 	VK_MEMORY_PROPERTY_HOST_CACHED_BIT(8), 	
 	VK_MEMORY_PROPERTY_LAZILY_ALLOCATED_BIT(16), 	
 	VK_MEMORY_PROPERTY_PROTECTED_BIT(32), 	
 	VK_MEMORY_PROPERTY_FLAG_BITS_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkMemoryPropertyFlag(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
