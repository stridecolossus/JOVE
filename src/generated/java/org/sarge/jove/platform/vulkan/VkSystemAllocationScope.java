package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkSystemAllocationScope implements IntegerEnumeration {
 	VK_SYSTEM_ALLOCATION_SCOPE_COMMAND(0), 	
 	VK_SYSTEM_ALLOCATION_SCOPE_OBJECT(1), 	
 	VK_SYSTEM_ALLOCATION_SCOPE_CACHE(2), 	
 	VK_SYSTEM_ALLOCATION_SCOPE_DEVICE(3), 	
 	VK_SYSTEM_ALLOCATION_SCOPE_INSTANCE(4), 	
 	VK_SYSTEM_ALLOCATION_SCOPE_BEGIN_RANGE(0), 	
 	VK_SYSTEM_ALLOCATION_SCOPE_END_RANGE(4), 	
 	VK_SYSTEM_ALLOCATION_SCOPE_RANGE_SIZE(5), 	
 	VK_SYSTEM_ALLOCATION_SCOPE_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkSystemAllocationScope(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
