package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkPipelineBindPoint implements IntEnum {
	GRAPHICS(0),
	COMPUTE(1),
	RAY_TRACING_KHR(1000165000),
	RAY_TRACING_NV(1000165000),
	MAX_ENUM(2147483647);

	private final int value;
	
	private VkPipelineBindPoint(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
