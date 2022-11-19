package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkCommandPoolResetFlag implements IntEnum {
 	RELEASE_RESOURCES(1);

	private final int value;

	private VkCommandPoolResetFlag(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
