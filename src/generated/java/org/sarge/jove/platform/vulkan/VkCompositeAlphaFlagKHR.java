package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkCompositeAlphaFlagKHR implements IntegerEnumeration {
 	VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR(1), 	
 	VK_COMPOSITE_ALPHA_PRE_MULTIPLIED_BIT_KHR(2), 	
 	VK_COMPOSITE_ALPHA_POST_MULTIPLIED_BIT_KHR(4), 	
 	VK_COMPOSITE_ALPHA_INHERIT_BIT_KHR(8), 	
 	VK_COMPOSITE_ALPHA_FLAG_BITS_MAX_ENUM_KHR(2147483647); 	

	private final int value;
	
	private VkCompositeAlphaFlagKHR(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
