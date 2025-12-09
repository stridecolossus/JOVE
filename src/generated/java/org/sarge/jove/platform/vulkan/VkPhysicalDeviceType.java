package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkPhysicalDeviceType implements IntEnum {
	OTHER(0),
	INTEGRATED_GPU(1),
	DISCRETE_GPU(2),
	VIRTUAL_GPU(3),
	CPU(4),
	MAX_ENUM(2147483647);

	private final int value;
	
	private VkPhysicalDeviceType(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
