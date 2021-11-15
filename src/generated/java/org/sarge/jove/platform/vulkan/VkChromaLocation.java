package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkChromaLocation implements IntegerEnumeration {
 	VK_CHROMA_LOCATION_COSITED_EVEN(0), 	
 	VK_CHROMA_LOCATION_MIDPOINT(1), 	
 	VK_CHROMA_LOCATION_COSITED_EVEN_KHR(0), 	
 	VK_CHROMA_LOCATION_MIDPOINT_KHR(1), 	
 	VK_CHROMA_LOCATION_BEGIN_RANGE(0), 	
 	VK_CHROMA_LOCATION_END_RANGE(1), 	
 	VK_CHROMA_LOCATION_RANGE_SIZE(2), 	
 	VK_CHROMA_LOCATION_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkChromaLocation(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
