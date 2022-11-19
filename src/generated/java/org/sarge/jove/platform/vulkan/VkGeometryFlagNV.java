package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkGeometryFlagNV implements IntEnum {
 	VK_GEOMETRY_OPAQUE_BIT_NV(1), 	
 	VK_GEOMETRY_NO_DUPLICATE_ANY_HIT_INVOCATION_BIT_NV(2), 	
 	VK_GEOMETRY_FLAG_BITS_MAX_ENUM_NV(2147483647); 	

	private final int value;
	
	private VkGeometryFlagNV(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
