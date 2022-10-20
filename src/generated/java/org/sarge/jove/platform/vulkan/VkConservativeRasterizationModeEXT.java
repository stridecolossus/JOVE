package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkConservativeRasterizationModeEXT implements IntegerEnumeration {
 	VK_CONSERVATIVE_RASTERIZATION_MODE_DISABLED_EXT(0), 	
 	VK_CONSERVATIVE_RASTERIZATION_MODE_OVERESTIMATE_EXT(1), 	
 	VK_CONSERVATIVE_RASTERIZATION_MODE_UNDERESTIMATE_EXT(2), 	
 	VK_CONSERVATIVE_RASTERIZATION_MODE_BEGIN_RANGE_EXT(0), 	
 	VK_CONSERVATIVE_RASTERIZATION_MODE_END_RANGE_EXT(2), 	
 	VK_CONSERVATIVE_RASTERIZATION_MODE_RANGE_SIZE_EXT(3), 	
 	VK_CONSERVATIVE_RASTERIZATION_MODE_MAX_ENUM_EXT(2147483647); 	

	private final int value;
	
	private VkConservativeRasterizationModeEXT(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
