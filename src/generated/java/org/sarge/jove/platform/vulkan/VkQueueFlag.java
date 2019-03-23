package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkQueueFlag implements IntegerEnumeration {
 	VK_QUEUE_GRAPHICS_BIT(1), 	
 	VK_QUEUE_COMPUTE_BIT(2), 	
 	VK_QUEUE_TRANSFER_BIT(4), 	
 	VK_QUEUE_SPARSE_BINDING_BIT(8), 	
 	VK_QUEUE_PROTECTED_BIT(16), 	
 	VK_QUEUE_FLAG_BITS_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkQueueFlag(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
