package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkValidationCheckEXT implements IntEnum {
 	VK_VALIDATION_CHECK_ALL_EXT(0), 	
 	VK_VALIDATION_CHECK_SHADERS_EXT(1), 	
 	VK_VALIDATION_CHECK_BEGIN_RANGE_EXT(0), 	
 	VK_VALIDATION_CHECK_END_RANGE_EXT(1), 	
 	VK_VALIDATION_CHECK_RANGE_SIZE_EXT(2), 	
 	VK_VALIDATION_CHECK_MAX_ENUM_EXT(2147483647); 	

	private final int value;
	
	private VkValidationCheckEXT(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
