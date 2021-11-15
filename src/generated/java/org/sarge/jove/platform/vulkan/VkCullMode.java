package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkCullMode implements IntegerEnumeration {
	NONE(0),
 	FRONT(1),
 	BACK(2),
 	FRONT_AND_BACK(3);

	private final int value;

	private VkCullMode(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
