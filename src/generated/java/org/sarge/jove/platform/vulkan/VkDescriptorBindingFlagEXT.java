package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkDescriptorBindingFlagEXT implements IntEnum {
 	VK_DESCRIPTOR_BINDING_UPDATE_AFTER_BIND_BIT_EXT(1), 	
 	VK_DESCRIPTOR_BINDING_UPDATE_UNUSED_WHILE_PENDING_BIT_EXT(2), 	
 	VK_DESCRIPTOR_BINDING_PARTIALLY_BOUND_BIT_EXT(4), 	
 	VK_DESCRIPTOR_BINDING_VARIABLE_DESCRIPTOR_COUNT_BIT_EXT(8), 	
 	VK_DESCRIPTOR_BINDING_FLAG_BITS_MAX_ENUM_EXT(2147483647); 	

	private final int value;
	
	private VkDescriptorBindingFlagEXT(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
