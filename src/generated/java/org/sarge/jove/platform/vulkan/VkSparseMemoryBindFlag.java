package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkSparseMemoryBindFlag implements IntegerEnumeration {
 	VK_SPARSE_MEMORY_BIND_METADATA_BIT(1), 	
 	VK_SPARSE_MEMORY_BIND_FLAG_BITS_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkSparseMemoryBindFlag(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
