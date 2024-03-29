package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkCompositeAlphaFlagKHR implements IntEnum {
 	OPAQUE(1),
 	PRE_MULTIPLIED(2),
 	POST_MULTIPLIED(4),
 	INHERIT(8);

	private final int value;

	private VkCompositeAlphaFlagKHR(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
