package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkPresentModeKHR implements IntegerEnumeration {
 	VK_PRESENT_MODE_IMMEDIATE_KHR(0), 	
 	VK_PRESENT_MODE_MAILBOX_KHR(1), 	
 	VK_PRESENT_MODE_FIFO_KHR(2), 	
 	VK_PRESENT_MODE_FIFO_RELAXED_KHR(3), 	
 	VK_PRESENT_MODE_SHARED_DEMAND_REFRESH_KHR(1000111000), 	
 	VK_PRESENT_MODE_SHARED_CONTINUOUS_REFRESH_KHR(1000111001), 	
 	VK_PRESENT_MODE_BEGIN_RANGE_KHR(0), 	
 	VK_PRESENT_MODE_END_RANGE_KHR(3), 	
 	VK_PRESENT_MODE_RANGE_SIZE_KHR(4), 	
 	VK_PRESENT_MODE_MAX_ENUM_KHR(2147483647); 	

	private final int value;
	
	private VkPresentModeKHR(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
