package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkFrontFace implements IntEnum {
 	COUNTER_CLOCKWISE(0),
 	CLOCKWISE(1);

	private final int value;

	private VkFrontFace(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
