package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkTessellationDomainOrigin implements IntEnum {
 	VK_TESSELLATION_DOMAIN_ORIGIN_UPPER_LEFT(0), 	
 	VK_TESSELLATION_DOMAIN_ORIGIN_LOWER_LEFT(1), 	
 	VK_TESSELLATION_DOMAIN_ORIGIN_UPPER_LEFT_KHR(0), 	
 	VK_TESSELLATION_DOMAIN_ORIGIN_LOWER_LEFT_KHR(1), 	
 	VK_TESSELLATION_DOMAIN_ORIGIN_BEGIN_RANGE(0), 	
 	VK_TESSELLATION_DOMAIN_ORIGIN_END_RANGE(1), 	
 	VK_TESSELLATION_DOMAIN_ORIGIN_RANGE_SIZE(2), 	
 	VK_TESSELLATION_DOMAIN_ORIGIN_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkTessellationDomainOrigin(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
