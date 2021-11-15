package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkCommandBufferLevel implements IntegerEnumeration {
 	PRIMARY(0),
 	SECONDARY(1);

	private final int value;

	private VkCommandBufferLevel(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
