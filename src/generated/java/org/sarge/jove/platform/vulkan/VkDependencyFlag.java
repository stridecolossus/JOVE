package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkDependencyFlag implements IntegerEnumeration {
 	VK_DEPENDENCY_BY_REGION_BIT(1), 	
 	VK_DEPENDENCY_DEVICE_GROUP_BIT(4), 	
 	VK_DEPENDENCY_VIEW_LOCAL_BIT(2), 	
 	VK_DEPENDENCY_VIEW_LOCAL_BIT_KHR(2), 	
 	VK_DEPENDENCY_DEVICE_GROUP_BIT_KHR(4), 	
 	VK_DEPENDENCY_FLAG_BITS_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkDependencyFlag(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
