package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkPresentModeKHR implements IntegerEnumeration {
 	IMMEDIATE_KHR(0),
 	MAILBOX_KHR(1),
 	FIFO_KHR(2),
 	FIFO_RELAXED_KHR(3),
 	SHARED_DEMAND_REFRESH_KHR(1000111000),
 	SHARED_CONTINUOUS_REFRESH_KHR(1000111001);

 	private final int value;

	private VkPresentModeKHR(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
