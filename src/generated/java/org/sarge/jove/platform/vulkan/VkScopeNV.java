package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkScopeNV implements IntEnum {
 	VK_SCOPE_DEVICE_NV(1), 	
 	VK_SCOPE_WORKGROUP_NV(2), 	
 	VK_SCOPE_SUBGROUP_NV(3), 	
 	VK_SCOPE_QUEUE_FAMILY_NV(5), 	
 	VK_SCOPE_BEGIN_RANGE_NV(1), 	
 	VK_SCOPE_END_RANGE_NV(5), 	
 	VK_SCOPE_RANGE_SIZE_NV(5), 	
 	VK_SCOPE_MAX_ENUM_NV(2147483647); 	

	private final int value;
	
	private VkScopeNV(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
