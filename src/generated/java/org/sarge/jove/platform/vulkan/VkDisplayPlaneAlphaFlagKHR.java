package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkDisplayPlaneAlphaFlagKHR implements IntegerEnumeration {
 	VK_DISPLAY_PLANE_ALPHA_OPAQUE_BIT_KHR(1), 	
 	VK_DISPLAY_PLANE_ALPHA_GLOBAL_BIT_KHR(2), 	
 	VK_DISPLAY_PLANE_ALPHA_PER_PIXEL_BIT_KHR(4), 	
 	VK_DISPLAY_PLANE_ALPHA_PER_PIXEL_PREMULTIPLIED_BIT_KHR(8), 	
 	VK_DISPLAY_PLANE_ALPHA_FLAG_BITS_MAX_ENUM_KHR(2147483647); 	

	private final int value;
	
	private VkDisplayPlaneAlphaFlagKHR(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
