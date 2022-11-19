package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkBuildAccelerationStructureFlagNV implements IntEnum {
 	VK_BUILD_ACCELERATION_STRUCTURE_ALLOW_UPDATE_BIT_NV(1), 	
 	VK_BUILD_ACCELERATION_STRUCTURE_ALLOW_COMPACTION_BIT_NV(2), 	
 	VK_BUILD_ACCELERATION_STRUCTURE_PREFER_FAST_TRACE_BIT_NV(4), 	
 	VK_BUILD_ACCELERATION_STRUCTURE_PREFER_FAST_BUILD_BIT_NV(8), 	
 	VK_BUILD_ACCELERATION_STRUCTURE_LOW_MEMORY_BIT_NV(16), 	
 	VK_BUILD_ACCELERATION_STRUCTURE_FLAG_BITS_MAX_ENUM_NV(2147483647); 	

	private final int value;
	
	private VkBuildAccelerationStructureFlagNV(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
