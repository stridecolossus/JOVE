package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkAccelerationStructureTypeNV implements IntegerEnumeration {
 	VK_ACCELERATION_STRUCTURE_TYPE_TOP_LEVEL_NV(0), 	
 	VK_ACCELERATION_STRUCTURE_TYPE_BOTTOM_LEVEL_NV(1), 	
 	VK_ACCELERATION_STRUCTURE_TYPE_BEGIN_RANGE_NV(0), 	
 	VK_ACCELERATION_STRUCTURE_TYPE_END_RANGE_NV(1), 	
 	VK_ACCELERATION_STRUCTURE_TYPE_RANGE_SIZE_NV(2), 	
 	VK_ACCELERATION_STRUCTURE_TYPE_MAX_ENUM_NV(2147483647); 	

	private final int value;
	
	private VkAccelerationStructureTypeNV(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
