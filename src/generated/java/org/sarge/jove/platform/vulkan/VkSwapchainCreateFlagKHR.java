package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkSwapchainCreateFlagKHR implements IntegerEnumeration {
 	VK_SWAPCHAIN_CREATE_SPLIT_INSTANCE_BIND_REGIONS_BIT_KHR(1), 	
 	VK_SWAPCHAIN_CREATE_PROTECTED_BIT_KHR(2), 	
 	VK_SWAPCHAIN_CREATE_MUTABLE_FORMAT_BIT_KHR(4), 	
 	VK_SWAPCHAIN_CREATE_FLAG_BITS_MAX_ENUM_KHR(2147483647); 	

	private final int value;
	
	private VkSwapchainCreateFlagKHR(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
