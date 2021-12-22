package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkPipelineCreateFlag implements IntegerEnumeration {
 	DISABLE_OPTIMIZATION(1),
 	ALLOW_DERIVATIVES(2),
 	DERIVATIVE(4),
 	VIEW_INDEX_FROM_DEVICE_INDEX(8),
 	DISPATCH_BASE(16),
 	DEFER_COMPILE_NV(32),
 	VIEW_INDEX_FROM_DEVICE_INDEX_KHR(8),
 	DISPATCH_BASE_KHR(16);

	private final int value;

	private VkPipelineCreateFlag(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
