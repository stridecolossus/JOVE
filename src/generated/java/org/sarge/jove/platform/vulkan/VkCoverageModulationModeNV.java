package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkCoverageModulationModeNV implements IntEnum {
	NONE_NV(0),
	RGB_NV(1),
	ALPHA_NV(2),
	RGBA_NV(3),
	MAX_ENUM_NV(2147483647);

	private final int value;
	
	private VkCoverageModulationModeNV(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
