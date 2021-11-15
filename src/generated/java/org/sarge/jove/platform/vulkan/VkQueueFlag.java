package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkQueueFlag implements IntegerEnumeration {
 	GRAPHICS(1),
 	COMPUTE(2),
 	TRANSFER(4),
 	SPARSE_BINDING(8),
 	PROTECTED(16);

	private final int value;

	private VkQueueFlag(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
