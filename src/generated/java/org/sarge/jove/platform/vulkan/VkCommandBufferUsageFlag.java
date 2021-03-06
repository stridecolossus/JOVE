package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkCommandBufferUsageFlag implements IntegerEnumeration {
 	VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT(1),
 	VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT(2),
 	VK_COMMAND_BUFFER_USAGE_SIMULTANEOUS_USE_BIT(4);

	private final int value;

	private VkCommandBufferUsageFlag(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
