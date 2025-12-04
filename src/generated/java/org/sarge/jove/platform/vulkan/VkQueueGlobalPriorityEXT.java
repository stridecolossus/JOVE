package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkQueueGlobalPriorityEXT implements IntEnum {
	LOW_EXT(128),
	MEDIUM_EXT(256),
	HIGH_EXT(512),
	REALTIME_EXT(1024),
	MAX_ENUM_EXT(2147483647);

	private final int value;
	
	private VkQueueGlobalPriorityEXT(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
