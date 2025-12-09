package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkTimeDomainEXT implements IntEnum {
	DEVICE_EXT(0),
	CLOCK_MONOTONIC_EXT(1),
	CLOCK_MONOTONIC_RAW_EXT(2),
	QUERY_PERFORMANCE_COUNTER_EXT(3),
	MAX_ENUM_EXT(2147483647);

	private final int value;
	
	private VkTimeDomainEXT(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
