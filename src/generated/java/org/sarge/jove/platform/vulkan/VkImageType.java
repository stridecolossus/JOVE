package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkImageType implements IntEnum {
 	ONE_D(0),
 	TWO_D(1),
 	THREE_D(2);

	private final int value;

	private VkImageType(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
