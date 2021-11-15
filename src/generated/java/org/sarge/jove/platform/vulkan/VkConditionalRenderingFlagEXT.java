package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkConditionalRenderingFlagEXT implements IntegerEnumeration {
 	VK_CONDITIONAL_RENDERING_INVERTED_BIT_EXT(1), 	
 	VK_CONDITIONAL_RENDERING_FLAG_BITS_MAX_ENUM_EXT(2147483647); 	

	private final int value;
	
	private VkConditionalRenderingFlagEXT(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
