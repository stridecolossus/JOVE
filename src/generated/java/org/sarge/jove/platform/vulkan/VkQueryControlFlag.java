package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkQueryControlFlag implements IntegerEnumeration {
 	PRECISE(1);

	private final int value;

	private VkQueryControlFlag(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
