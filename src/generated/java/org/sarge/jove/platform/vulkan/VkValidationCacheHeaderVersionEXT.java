package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkValidationCacheHeaderVersionEXT implements IntEnum {
 	VK_VALIDATION_CACHE_HEADER_VERSION_ONE_EXT(1), 	
 	VK_VALIDATION_CACHE_HEADER_VERSION_BEGIN_RANGE_EXT(1), 	
 	VK_VALIDATION_CACHE_HEADER_VERSION_END_RANGE_EXT(1), 	
 	VK_VALIDATION_CACHE_HEADER_VERSION_RANGE_SIZE_EXT(1), 	
 	VK_VALIDATION_CACHE_HEADER_VERSION_MAX_ENUM_EXT(2147483647); 	

	private final int value;
	
	private VkValidationCacheHeaderVersionEXT(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
