package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkSamplerYcbcrRange implements IntEnum {
	ITU_FULL(0),
	ITU_NARROW(1),
	ITU_FULL_KHR(0),
	ITU_NARROW_KHR(1),
	MAX_ENUM(2147483647);

	private final int value;
	
	private VkSamplerYcbcrRange(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
