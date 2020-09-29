package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkDeviceGroupPresentModeFlagKHR implements IntegerEnumeration {
 	VK_DEVICE_GROUP_PRESENT_MODE_LOCAL_BIT_KHR(1), 	
 	VK_DEVICE_GROUP_PRESENT_MODE_REMOTE_BIT_KHR(2), 	
 	VK_DEVICE_GROUP_PRESENT_MODE_SUM_BIT_KHR(4), 	
 	VK_DEVICE_GROUP_PRESENT_MODE_LOCAL_MULTI_DEVICE_BIT_KHR(8), 	
 	VK_DEVICE_GROUP_PRESENT_MODE_FLAG_BITS_MAX_ENUM_KHR(2147483647); 	

	private final int value;
	
	private VkDeviceGroupPresentModeFlagKHR(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
