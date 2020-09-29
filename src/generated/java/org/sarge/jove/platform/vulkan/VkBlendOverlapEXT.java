package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkBlendOverlapEXT implements IntegerEnumeration {
 	VK_BLEND_OVERLAP_UNCORRELATED_EXT(0), 	
 	VK_BLEND_OVERLAP_DISJOINT_EXT(1), 	
 	VK_BLEND_OVERLAP_CONJOINT_EXT(2), 	
 	VK_BLEND_OVERLAP_BEGIN_RANGE_EXT(0), 	
 	VK_BLEND_OVERLAP_END_RANGE_EXT(2), 	
 	VK_BLEND_OVERLAP_RANGE_SIZE_EXT(3), 	
 	VK_BLEND_OVERLAP_MAX_ENUM_EXT(2147483647); 	

	private final int value;
	
	private VkBlendOverlapEXT(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
