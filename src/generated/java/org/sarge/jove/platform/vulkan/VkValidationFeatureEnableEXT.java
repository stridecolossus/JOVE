package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkValidationFeatureEnableEXT implements IntegerEnumeration {
 	VK_VALIDATION_FEATURE_ENABLE_GPU_ASSISTED_EXT(0), 	
 	VK_VALIDATION_FEATURE_ENABLE_GPU_ASSISTED_RESERVE_BINDING_SLOT_EXT(1), 	
 	VK_VALIDATION_FEATURE_ENABLE_BEGIN_RANGE_EXT(0), 	
 	VK_VALIDATION_FEATURE_ENABLE_END_RANGE_EXT(1), 	
 	VK_VALIDATION_FEATURE_ENABLE_RANGE_SIZE_EXT(2), 	
 	VK_VALIDATION_FEATURE_ENABLE_MAX_ENUM_EXT(2147483647); 	

	private final int value;
	
	private VkValidationFeatureEnableEXT(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
