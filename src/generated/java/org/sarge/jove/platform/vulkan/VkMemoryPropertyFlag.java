package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkMemoryPropertyFlag implements IntegerEnumeration {
 	DEVICE_LOCAL(1),
 	HOST_VISIBLE(2),
 	HOST_COHERENT(4),
 	HOST_CACHED(8),
 	LAZILY_ALLOCATED(16),
 	PROTECTED(32);

	private final int value;

	private VkMemoryPropertyFlag(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
