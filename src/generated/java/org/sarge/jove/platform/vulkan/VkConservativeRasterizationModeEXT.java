package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkConservativeRasterizationModeEXT implements IntEnum {
	DISABLED_EXT(0),
	OVERESTIMATE_EXT(1),
	UNDERESTIMATE_EXT(2),
	MAX_ENUM_EXT(2147483647);

	private final int value;
	
	private VkConservativeRasterizationModeEXT(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
