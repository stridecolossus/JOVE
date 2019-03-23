package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkPipelineBindPoint implements IntegerEnumeration {
 	VK_PIPELINE_BIND_POINT_GRAPHICS(0), 	
 	VK_PIPELINE_BIND_POINT_COMPUTE(1), 	
 	VK_PIPELINE_BIND_POINT_RAY_TRACING_NV(1000165000), 	
 	VK_PIPELINE_BIND_POINT_BEGIN_RANGE(0), 	
 	VK_PIPELINE_BIND_POINT_END_RANGE(1), 	
 	VK_PIPELINE_BIND_POINT_RANGE_SIZE(2), 	
 	VK_PIPELINE_BIND_POINT_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkPipelineBindPoint(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
