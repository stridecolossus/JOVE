package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkSampleCountFlag implements IntegerEnumeration {
 	VK_SAMPLE_COUNT_1(1),
 	VK_SAMPLE_COUNT_2(2),
 	VK_SAMPLE_COUNT_4(4),
 	VK_SAMPLE_COUNT_8(8),
 	VK_SAMPLE_COUNT_16(16),
 	VK_SAMPLE_COUNT_32(32),
 	VK_SAMPLE_COUNT_64(64);

	private final int value;

	private VkSampleCountFlag(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
