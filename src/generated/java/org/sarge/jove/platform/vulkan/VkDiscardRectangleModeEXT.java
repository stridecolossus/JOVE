package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkDiscardRectangleModeEXT implements IntegerEnumeration {
 	VK_DISCARD_RECTANGLE_MODE_INCLUSIVE_EXT(0), 	
 	VK_DISCARD_RECTANGLE_MODE_EXCLUSIVE_EXT(1), 	
 	VK_DISCARD_RECTANGLE_MODE_BEGIN_RANGE_EXT(0), 	
 	VK_DISCARD_RECTANGLE_MODE_END_RANGE_EXT(1), 	
 	VK_DISCARD_RECTANGLE_MODE_RANGE_SIZE_EXT(2), 	
 	VK_DISCARD_RECTANGLE_MODE_MAX_ENUM_EXT(2147483647); 	

	private final int value;
	
	private VkDiscardRectangleModeEXT(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
