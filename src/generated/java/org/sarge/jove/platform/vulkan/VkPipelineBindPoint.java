package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkPipelineBindPoint implements IntegerEnumeration {
 	GRAPHICS(0),
 	COMPUTE(1),
 	RAY_TRACING_NV(1000165000);

	private final int value;

	private VkPipelineBindPoint(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
