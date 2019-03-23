package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkCopyAccelerationStructureModeNV implements IntegerEnumeration {
 	VK_COPY_ACCELERATION_STRUCTURE_MODE_CLONE_NV(0), 	
 	VK_COPY_ACCELERATION_STRUCTURE_MODE_COMPACT_NV(1), 	
 	VK_COPY_ACCELERATION_STRUCTURE_MODE_BEGIN_RANGE_NV(0), 	
 	VK_COPY_ACCELERATION_STRUCTURE_MODE_END_RANGE_NV(1), 	
 	VK_COPY_ACCELERATION_STRUCTURE_MODE_RANGE_SIZE_NV(2), 	
 	VK_COPY_ACCELERATION_STRUCTURE_MODE_MAX_ENUM_NV(2147483647); 	

	private final int value;
	
	private VkCopyAccelerationStructureModeNV(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
