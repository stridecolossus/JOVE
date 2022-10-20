package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkGeometryTypeNV implements IntegerEnumeration {
 	VK_GEOMETRY_TYPE_TRIANGLES_NV(0), 	
 	VK_GEOMETRY_TYPE_AABBS_NV(1), 	
 	VK_GEOMETRY_TYPE_BEGIN_RANGE_NV(0), 	
 	VK_GEOMETRY_TYPE_END_RANGE_NV(1), 	
 	VK_GEOMETRY_TYPE_RANGE_SIZE_NV(2), 	
 	VK_GEOMETRY_TYPE_MAX_ENUM_NV(2147483647); 	

	private final int value;
	
	private VkGeometryTypeNV(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
