package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkMemoryHeapFlag implements IntegerEnumeration {
 	DEVICE_LOCAL(1),
 	MULTI_INSTANCE(2),
 	MULTI_INSTANCE_KHR(2);

	private final int value;

	private VkMemoryHeapFlag(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
