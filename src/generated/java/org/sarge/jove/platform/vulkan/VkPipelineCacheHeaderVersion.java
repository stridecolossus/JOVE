package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkPipelineCacheHeaderVersion implements IntegerEnumeration {
 	VK_PIPELINE_CACHE_HEADER_VERSION_ONE(1), 	
 	VK_PIPELINE_CACHE_HEADER_VERSION_BEGIN_RANGE(1), 	
 	VK_PIPELINE_CACHE_HEADER_VERSION_END_RANGE(1), 	
 	VK_PIPELINE_CACHE_HEADER_VERSION_RANGE_SIZE(1), 	
 	VK_PIPELINE_CACHE_HEADER_VERSION_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkPipelineCacheHeaderVersion(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
