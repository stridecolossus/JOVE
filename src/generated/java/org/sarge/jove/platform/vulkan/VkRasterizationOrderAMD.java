package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkRasterizationOrderAMD implements IntegerEnumeration {
 	VK_RASTERIZATION_ORDER_STRICT_AMD(0), 	
 	VK_RASTERIZATION_ORDER_RELAXED_AMD(1), 	
 	VK_RASTERIZATION_ORDER_BEGIN_RANGE_AMD(0), 	
 	VK_RASTERIZATION_ORDER_END_RANGE_AMD(1), 	
 	VK_RASTERIZATION_ORDER_RANGE_SIZE_AMD(2), 	
 	VK_RASTERIZATION_ORDER_MAX_ENUM_AMD(2147483647); 	

	private final int value;
	
	private VkRasterizationOrderAMD(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
