package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkFilter implements IntegerEnumeration {
 	NEAREST(0),
 	LINEAR(1),
 	CUBIC_IMG(1000015000),
 	CUBIC_EXT(1000015000);

	private final int value;

	private VkFilter(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
