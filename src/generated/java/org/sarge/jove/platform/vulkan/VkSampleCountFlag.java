package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkSampleCountFlag implements IntegerEnumeration {
 	COUNT_1(1),
 	COUNT_2(2),
 	COUNT_4(4),
 	COUNT_8(8),
 	COUNT_16(16),
 	COUNT_32(32),
 	COUNT_64(64);

	private final int value;

	private VkSampleCountFlag(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
