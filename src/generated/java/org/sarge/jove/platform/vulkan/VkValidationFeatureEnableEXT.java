package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkValidationFeatureEnableEXT implements IntEnum {
	GPU_ASSISTED_EXT(0),
	GPU_ASSISTED_RESERVE_BINDING_SLOT_EXT(1),
	BEST_PRACTICES_EXT(2),
	DEBUG_PRINTF_EXT(3),
	SYNCHRONIZATION_VALIDATION_EXT(4),
	MAX_ENUM_EXT(2147483647);

	private final int value;
	
	private VkValidationFeatureEnableEXT(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
