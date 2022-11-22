package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkSwapchainCreateFlagKHR implements IntEnum {
 	VK_SWAPCHAIN_CREATE_SPLIT_INSTANCE_BIND_REGIONS_BIT_KHR(1),
 	VK_SWAPCHAIN_CREATE_PROTECTED_BIT_KHR(2),
 	VK_SWAPCHAIN_CREATE_MUTABLE_FORMAT_BIT_KHR(4);

	private final int value;

	private VkSwapchainCreateFlagKHR(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
