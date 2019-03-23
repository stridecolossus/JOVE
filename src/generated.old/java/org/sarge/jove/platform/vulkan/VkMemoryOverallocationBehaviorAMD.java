package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkMemoryOverallocationBehaviorAMD implements IntegerEnumeration {
 	VK_MEMORY_OVERALLOCATION_BEHAVIOR_DEFAULT_AMD(0), 	
 	VK_MEMORY_OVERALLOCATION_BEHAVIOR_ALLOWED_AMD(1), 	
 	VK_MEMORY_OVERALLOCATION_BEHAVIOR_DISALLOWED_AMD(2), 	
 	VK_MEMORY_OVERALLOCATION_BEHAVIOR_BEGIN_RANGE_AMD(0), 	
 	VK_MEMORY_OVERALLOCATION_BEHAVIOR_END_RANGE_AMD(2), 	
 	VK_MEMORY_OVERALLOCATION_BEHAVIOR_RANGE_SIZE_AMD(3), 	
 	VK_MEMORY_OVERALLOCATION_BEHAVIOR_MAX_ENUM_AMD(2147483647); 	

	private final int value;
	
	private VkMemoryOverallocationBehaviorAMD(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
