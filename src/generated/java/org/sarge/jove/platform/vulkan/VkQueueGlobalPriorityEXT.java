package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkQueueGlobalPriorityEXT implements IntegerEnumeration {
 	VK_QUEUE_GLOBAL_PRIORITY_LOW_EXT(128), 	
 	VK_QUEUE_GLOBAL_PRIORITY_MEDIUM_EXT(256), 	
 	VK_QUEUE_GLOBAL_PRIORITY_HIGH_EXT(512), 	
 	VK_QUEUE_GLOBAL_PRIORITY_REALTIME_EXT(1024), 	
 	VK_QUEUE_GLOBAL_PRIORITY_BEGIN_RANGE_EXT(128), 	
 	VK_QUEUE_GLOBAL_PRIORITY_END_RANGE_EXT(1024), 	
 	VK_QUEUE_GLOBAL_PRIORITY_RANGE_SIZE_EXT(897), 	
 	VK_QUEUE_GLOBAL_PRIORITY_MAX_ENUM_EXT(2147483647); 	

	private final int value;
	
	private VkQueueGlobalPriorityEXT(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
