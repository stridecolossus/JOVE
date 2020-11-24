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
 	VK_PRESENT_MODE_SHARED_CONTINUOUS_REFRESH_KHR(1000111001);

 	private final int value;

	private VkPresentModeKHR(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
