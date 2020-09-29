package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkCoarseSampleOrderTypeNV implements IntegerEnumeration {
 	VK_COARSE_SAMPLE_ORDER_TYPE_DEFAULT_NV(0), 	
 	VK_COARSE_SAMPLE_ORDER_TYPE_CUSTOM_NV(1), 	
 	VK_COARSE_SAMPLE_ORDER_TYPE_PIXEL_MAJOR_NV(2), 	
 	VK_COARSE_SAMPLE_ORDER_TYPE_SAMPLE_MAJOR_NV(3), 	
 	VK_COARSE_SAMPLE_ORDER_TYPE_BEGIN_RANGE_NV(0), 	
 	VK_COARSE_SAMPLE_ORDER_TYPE_END_RANGE_NV(3), 	
 	VK_COARSE_SAMPLE_ORDER_TYPE_RANGE_SIZE_NV(4), 	
 	VK_COARSE_SAMPLE_ORDER_TYPE_MAX_ENUM_NV(2147483647); 	

	private final int value;
	
	private VkCoarseSampleOrderTypeNV(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
