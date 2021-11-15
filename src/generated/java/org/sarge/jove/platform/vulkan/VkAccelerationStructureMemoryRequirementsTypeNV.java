package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkAccelerationStructureMemoryRequirementsTypeNV implements IntegerEnumeration {
 	VK_ACCELERATION_STRUCTURE_MEMORY_REQUIREMENTS_TYPE_OBJECT_NV(0), 	
 	VK_ACCELERATION_STRUCTURE_MEMORY_REQUIREMENTS_TYPE_BUILD_SCRATCH_NV(1), 	
 	VK_ACCELERATION_STRUCTURE_MEMORY_REQUIREMENTS_TYPE_UPDATE_SCRATCH_NV(2), 	
 	VK_ACCELERATION_STRUCTURE_MEMORY_REQUIREMENTS_TYPE_BEGIN_RANGE_NV(0), 	
 	VK_ACCELERATION_STRUCTURE_MEMORY_REQUIREMENTS_TYPE_END_RANGE_NV(2), 	
 	VK_ACCELERATION_STRUCTURE_MEMORY_REQUIREMENTS_TYPE_RANGE_SIZE_NV(3), 	
 	VK_ACCELERATION_STRUCTURE_MEMORY_REQUIREMENTS_TYPE_MAX_ENUM_NV(2147483647); 	

	private final int value;
	
	private VkAccelerationStructureMemoryRequirementsTypeNV(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
