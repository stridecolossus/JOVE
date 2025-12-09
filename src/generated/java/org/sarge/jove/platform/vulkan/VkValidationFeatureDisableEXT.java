package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkValidationFeatureDisableEXT implements IntEnum {
	ALL_EXT(0),
	SHADERS_EXT(1),
	THREAD_SAFETY_EXT(2),
	API_PARAMETERS_EXT(3),
	OBJECT_LIFETIMES_EXT(4),
	CORE_CHECKS_EXT(5),
	UNIQUE_HANDLES_EXT(6),
	MAX_ENUM_EXT(2147483647);

	private final int value;
	
	private VkValidationFeatureDisableEXT(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
