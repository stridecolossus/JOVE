package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkFilter implements IntegerEnumeration {
 	VK_FILTER_NEAREST(0), 	
 	VK_FILTER_LINEAR(1), 	
 	VK_FILTER_CUBIC_IMG(1000015000), 	
 	VK_FILTER_CUBIC_EXT(1000015000), 	
 	VK_FILTER_BEGIN_RANGE(0), 	
 	VK_FILTER_END_RANGE(1), 	
 	VK_FILTER_RANGE_SIZE(2), 	
 	VK_FILTER_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkFilter(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
