package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkSurfaceTransformFlagKHR implements IntegerEnumeration {
 	VK_SURFACE_TRANSFORM_IDENTITY_BIT_KHR(1), 	
 	VK_SURFACE_TRANSFORM_ROTATE_90_BIT_KHR(2), 	
 	VK_SURFACE_TRANSFORM_ROTATE_180_BIT_KHR(4), 	
 	VK_SURFACE_TRANSFORM_ROTATE_270_BIT_KHR(8), 	
 	VK_SURFACE_TRANSFORM_HORIZONTAL_MIRROR_BIT_KHR(16), 	
 	VK_SURFACE_TRANSFORM_HORIZONTAL_MIRROR_ROTATE_90_BIT_KHR(32), 	
 	VK_SURFACE_TRANSFORM_HORIZONTAL_MIRROR_ROTATE_180_BIT_KHR(64), 	
 	VK_SURFACE_TRANSFORM_HORIZONTAL_MIRROR_ROTATE_270_BIT_KHR(128), 	
 	VK_SURFACE_TRANSFORM_INHERIT_BIT_KHR(256), 	
 	VK_SURFACE_TRANSFORM_FLAG_BITS_MAX_ENUM_KHR(2147483647); 	

	private final int value;
	
	private VkSurfaceTransformFlagKHR(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
