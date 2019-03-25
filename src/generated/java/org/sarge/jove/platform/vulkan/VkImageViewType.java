package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkImageViewType implements IntegerEnumeration {
 	VK_IMAGE_VIEW_TYPE_1D(0), 	
 	VK_IMAGE_VIEW_TYPE_2D(1), 	
 	VK_IMAGE_VIEW_TYPE_3D(2), 	
 	VK_IMAGE_VIEW_TYPE_CUBE(3), 	
 	VK_IMAGE_VIEW_TYPE_1D_ARRAY(4), 	
 	VK_IMAGE_VIEW_TYPE_2D_ARRAY(5), 	
 	VK_IMAGE_VIEW_TYPE_CUBE_ARRAY(6), 	
 	VK_IMAGE_VIEW_TYPE_BEGIN_RANGE(0), 	
 	VK_IMAGE_VIEW_TYPE_END_RANGE(6), 	
 	VK_IMAGE_VIEW_TYPE_RANGE_SIZE(7), 	
 	VK_IMAGE_VIEW_TYPE_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkImageViewType(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}