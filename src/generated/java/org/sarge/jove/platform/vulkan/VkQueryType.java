package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkQueryType implements IntegerEnumeration {
 	VK_QUERY_TYPE_OCCLUSION(0), 	
 	VK_QUERY_TYPE_PIPELINE_STATISTICS(1), 	
 	VK_QUERY_TYPE_TIMESTAMP(2), 	
 	VK_QUERY_TYPE_TRANSFORM_FEEDBACK_STREAM_EXT(1000028004), 	
 	VK_QUERY_TYPE_ACCELERATION_STRUCTURE_COMPACTED_SIZE_NV(1000165000), 	
 	VK_QUERY_TYPE_BEGIN_RANGE(0), 	
 	VK_QUERY_TYPE_END_RANGE(2), 	
 	VK_QUERY_TYPE_RANGE_SIZE(3), 	
 	VK_QUERY_TYPE_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkQueryType(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
