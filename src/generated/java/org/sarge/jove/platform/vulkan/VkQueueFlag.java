package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkQueueFlag implements IntEnum {
 	GRAPHICS(1),
 	COMPUTE(2),
 	TRANSFER(4),
 	SPARSE_BINDING(8),
 	PROTECTED(16),
	DECODE_KHR(32),
    VIDEO_ENCODE_KHR(64),
    OPTICAL_FLOW_NV(128);

    private final int value;

	private VkQueueFlag(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
