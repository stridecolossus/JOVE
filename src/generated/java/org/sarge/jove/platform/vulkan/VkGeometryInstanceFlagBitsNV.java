package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkGeometryInstanceFlagBitsNV implements IntegerEnumeration {
 	VK_GEOMETRY_INSTANCE_TRIANGLE_CULL_DISABLE_BIT_NV(1), 	
 	VK_GEOMETRY_INSTANCE_TRIANGLE_FRONT_COUNTERCLOCKWISE_BIT_NV(2), 	
 	VK_GEOMETRY_INSTANCE_FORCE_OPAQUE_BIT_NV(4), 	
 	VK_GEOMETRY_INSTANCE_FORCE_NO_OPAQUE_BIT_NV(8), 	
 	VK_GEOMETRY_INSTANCE_FLAG_BITS_MAX_ENUM_NV(2147483647); 	

	private final int value;
	
	private VkGeometryInstanceFlagBitsNV(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
