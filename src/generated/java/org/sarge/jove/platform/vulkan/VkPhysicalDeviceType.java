package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkPhysicalDeviceType implements IntegerEnumeration {
 	VK_PHYSICAL_DEVICE_TYPE_OTHER(0), 	
 	VK_PHYSICAL_DEVICE_TYPE_INTEGRATED_GPU(1), 	
 	VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU(2), 	
 	VK_PHYSICAL_DEVICE_TYPE_VIRTUAL_GPU(3), 	
 	VK_PHYSICAL_DEVICE_TYPE_CPU(4), 	
 	VK_PHYSICAL_DEVICE_TYPE_BEGIN_RANGE(0), 	
 	VK_PHYSICAL_DEVICE_TYPE_END_RANGE(4), 	
 	VK_PHYSICAL_DEVICE_TYPE_RANGE_SIZE(5), 	
 	VK_PHYSICAL_DEVICE_TYPE_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkPhysicalDeviceType(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
