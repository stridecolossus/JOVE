package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkInternalAllocationType implements IntEnum {
 	VK_INTERNAL_ALLOCATION_TYPE_EXECUTABLE(0), 	
 	VK_INTERNAL_ALLOCATION_TYPE_BEGIN_RANGE(0), 	
 	VK_INTERNAL_ALLOCATION_TYPE_END_RANGE(0), 	
 	VK_INTERNAL_ALLOCATION_TYPE_RANGE_SIZE(1), 	
 	VK_INTERNAL_ALLOCATION_TYPE_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkInternalAllocationType(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
