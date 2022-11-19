package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkViewportCoordinateSwizzleNV implements IntEnum {
 	VK_VIEWPORT_COORDINATE_SWIZZLE_POSITIVE_X_NV(0), 	
 	VK_VIEWPORT_COORDINATE_SWIZZLE_NEGATIVE_X_NV(1), 	
 	VK_VIEWPORT_COORDINATE_SWIZZLE_POSITIVE_Y_NV(2), 	
 	VK_VIEWPORT_COORDINATE_SWIZZLE_NEGATIVE_Y_NV(3), 	
 	VK_VIEWPORT_COORDINATE_SWIZZLE_POSITIVE_Z_NV(4), 	
 	VK_VIEWPORT_COORDINATE_SWIZZLE_NEGATIVE_Z_NV(5), 	
 	VK_VIEWPORT_COORDINATE_SWIZZLE_POSITIVE_W_NV(6), 	
 	VK_VIEWPORT_COORDINATE_SWIZZLE_NEGATIVE_W_NV(7), 	
 	VK_VIEWPORT_COORDINATE_SWIZZLE_BEGIN_RANGE_NV(0), 	
 	VK_VIEWPORT_COORDINATE_SWIZZLE_END_RANGE_NV(7), 	
 	VK_VIEWPORT_COORDINATE_SWIZZLE_RANGE_SIZE_NV(8), 	
 	VK_VIEWPORT_COORDINATE_SWIZZLE_MAX_ENUM_NV(2147483647); 	

	private final int value;
	
	private VkViewportCoordinateSwizzleNV(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
