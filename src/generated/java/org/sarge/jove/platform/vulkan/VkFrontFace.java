package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkFrontFace implements IntegerEnumeration {
 	VK_FRONT_FACE_COUNTER_CLOCKWISE(0), 	
 	VK_FRONT_FACE_CLOCKWISE(1), 	
 	VK_FRONT_FACE_BEGIN_RANGE(0), 	
 	VK_FRONT_FACE_END_RANGE(1), 	
 	VK_FRONT_FACE_RANGE_SIZE(2), 	
 	VK_FRONT_FACE_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkFrontFace(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
