package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkSurfaceCounterFlagEXT implements IntegerEnumeration {
 	VK_SURFACE_COUNTER_VBLANK_EXT(1), 	
 	VK_SURFACE_COUNTER_FLAG_BITS_MAX_ENUM_EXT(2147483647); 	

	private final int value;
	
	private VkSurfaceCounterFlagEXT(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
