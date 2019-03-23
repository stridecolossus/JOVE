package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkImageType implements IntegerEnumeration {
 	VK_IMAGE_TYPE_1D(0), 	
 	VK_IMAGE_TYPE_2D(1), 	
 	VK_IMAGE_TYPE_3D(2), 	
 	VK_IMAGE_TYPE_BEGIN_RANGE(0), 	
 	VK_IMAGE_TYPE_END_RANGE(2), 	
 	VK_IMAGE_TYPE_RANGE_SIZE(3), 	
 	VK_IMAGE_TYPE_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkImageType(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
