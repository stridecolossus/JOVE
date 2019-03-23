package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkBufferCreateFlag implements IntegerEnumeration {
 	VK_BUFFER_CREATE_SPARSE_BINDING_BIT(1), 	
 	VK_BUFFER_CREATE_SPARSE_RESIDENCY_BIT(2), 	
 	VK_BUFFER_CREATE_SPARSE_ALIASED_BIT(4), 	
 	VK_BUFFER_CREATE_PROTECTED_BIT(8), 	
 	VK_BUFFER_CREATE_DEVICE_ADDRESS_CAPTURE_REPLAY_BIT_EXT(16), 	
 	VK_BUFFER_CREATE_FLAG_BITS_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkBufferCreateFlag(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
