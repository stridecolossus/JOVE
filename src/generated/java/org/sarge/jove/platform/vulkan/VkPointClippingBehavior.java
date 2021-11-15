package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkPointClippingBehavior implements IntegerEnumeration {
 	VK_POINT_CLIPPING_BEHAVIOR_ALL_CLIP_PLANES(0), 	
 	VK_POINT_CLIPPING_BEHAVIOR_USER_CLIP_PLANES_ONLY(1), 	
 	VK_POINT_CLIPPING_BEHAVIOR_ALL_CLIP_PLANES_KHR(0), 	
 	VK_POINT_CLIPPING_BEHAVIOR_USER_CLIP_PLANES_ONLY_KHR(1), 	
 	VK_POINT_CLIPPING_BEHAVIOR_BEGIN_RANGE(0), 	
 	VK_POINT_CLIPPING_BEHAVIOR_END_RANGE(1), 	
 	VK_POINT_CLIPPING_BEHAVIOR_RANGE_SIZE(2), 	
 	VK_POINT_CLIPPING_BEHAVIOR_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkPointClippingBehavior(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
