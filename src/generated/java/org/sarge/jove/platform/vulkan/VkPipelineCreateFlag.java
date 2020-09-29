package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkPipelineCreateFlag implements IntegerEnumeration {
 	VK_PIPELINE_CREATE_DISABLE_OPTIMIZATION_BIT(1), 	
 	VK_PIPELINE_CREATE_ALLOW_DERIVATIVES_BIT(2), 	
 	VK_PIPELINE_CREATE_DERIVATIVE_BIT(4), 	
 	VK_PIPELINE_CREATE_VIEW_INDEX_FROM_DEVICE_INDEX_BIT(8), 	
 	VK_PIPELINE_CREATE_DISPATCH_BASE(16), 	
 	VK_PIPELINE_CREATE_DEFER_COMPILE_BIT_NV(32), 	
 	VK_PIPELINE_CREATE_VIEW_INDEX_FROM_DEVICE_INDEX_BIT_KHR(8), 	
 	VK_PIPELINE_CREATE_DISPATCH_BASE_KHR(16), 	
 	VK_PIPELINE_CREATE_FLAG_BITS_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkPipelineCreateFlag(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
