package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkImageAspectFlag implements IntegerEnumeration {
 	VK_IMAGE_ASPECT_COLOR_BIT(1), 	
 	VK_IMAGE_ASPECT_DEPTH_BIT(2), 	
 	VK_IMAGE_ASPECT_STENCIL_BIT(4), 	
 	VK_IMAGE_ASPECT_METADATA_BIT(8), 	
 	VK_IMAGE_ASPECT_PLANE_0_BIT(16), 	
 	VK_IMAGE_ASPECT_PLANE_1_BIT(32), 	
 	VK_IMAGE_ASPECT_PLANE_2_BIT(64), 	
 	VK_IMAGE_ASPECT_MEMORY_PLANE_0_BIT_EXT(128), 	
 	VK_IMAGE_ASPECT_MEMORY_PLANE_1_BIT_EXT(256), 	
 	VK_IMAGE_ASPECT_MEMORY_PLANE_2_BIT_EXT(512), 	
 	VK_IMAGE_ASPECT_MEMORY_PLANE_3_BIT_EXT(1024), 	
 	VK_IMAGE_ASPECT_PLANE_0_BIT_KHR(16), 	
 	VK_IMAGE_ASPECT_PLANE_1_BIT_KHR(32), 	
 	VK_IMAGE_ASPECT_PLANE_2_BIT_KHR(64), 	
 	VK_IMAGE_ASPECT_FLAG_BITS_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkImageAspectFlag(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
