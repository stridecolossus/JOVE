package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkValidationFeatureDisableEXT implements IntegerEnumeration {
 	VK_VALIDATION_FEATURE_DISABLE_ALL_EXT(0), 	
 	VK_VALIDATION_FEATURE_DISABLE_SHADERS_EXT(1), 	
 	VK_VALIDATION_FEATURE_DISABLE_THREAD_SAFETY_EXT(2), 	
 	VK_VALIDATION_FEATURE_DISABLE_API_PARAMETERS_EXT(3), 	
 	VK_VALIDATION_FEATURE_DISABLE_OBJECT_LIFETIMES_EXT(4), 	
 	VK_VALIDATION_FEATURE_DISABLE_CORE_CHECKS_EXT(5), 	
 	VK_VALIDATION_FEATURE_DISABLE_UNIQUE_HANDLES_EXT(6), 	
 	VK_VALIDATION_FEATURE_DISABLE_BEGIN_RANGE_EXT(0), 	
 	VK_VALIDATION_FEATURE_DISABLE_END_RANGE_EXT(6), 	
 	VK_VALIDATION_FEATURE_DISABLE_RANGE_SIZE_EXT(7), 	
 	VK_VALIDATION_FEATURE_DISABLE_MAX_ENUM_EXT(2147483647); 	

	private final int value;
	
	private VkValidationFeatureDisableEXT(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
