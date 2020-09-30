package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkPipelineBindPoint implements IntegerEnumeration {
 	VK_PIPELINE_BIND_POINT_GRAPHICS(0),
 	VK_PIPELINE_BIND_POINT_COMPUTE(1),
 	VK_PIPELINE_BIND_POINT_RAY_TRACING_NV(1000165000);

	private final int value;

	private VkPipelineBindPoint(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
