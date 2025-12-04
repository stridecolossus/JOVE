package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkCoarseSampleOrderTypeNV implements IntEnum {
	DEFAULT_NV(0),
	CUSTOM_NV(1),
	PIXEL_MAJOR_NV(2),
	SAMPLE_MAJOR_NV(3),
	MAX_ENUM_NV(2147483647);

	private final int value;
	
	private VkCoarseSampleOrderTypeNV(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
