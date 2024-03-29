package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkDeviceQueueCreateFlag implements IntEnum {
 	VK_DEVICE_QUEUE_CREATE_PROTECTED_BIT(1);

	private final int value;

	private VkDeviceQueueCreateFlag(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
