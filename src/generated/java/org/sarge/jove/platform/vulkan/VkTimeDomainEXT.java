package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkTimeDomainEXT implements IntEnum {
 	VK_TIME_DOMAIN_DEVICE_EXT(0), 	
 	VK_TIME_DOMAIN_CLOCK_MONOTONIC_EXT(1), 	
 	VK_TIME_DOMAIN_CLOCK_MONOTONIC_RAW_EXT(2), 	
 	VK_TIME_DOMAIN_QUERY_PERFORMANCE_COUNTER_EXT(3), 	
 	VK_TIME_DOMAIN_BEGIN_RANGE_EXT(0), 	
 	VK_TIME_DOMAIN_END_RANGE_EXT(3), 	
 	VK_TIME_DOMAIN_RANGE_SIZE_EXT(4), 	
 	VK_TIME_DOMAIN_MAX_ENUM_EXT(2147483647); 	

	private final int value;
	
	private VkTimeDomainEXT(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
