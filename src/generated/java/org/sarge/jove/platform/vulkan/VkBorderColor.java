package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkBorderColor implements IntegerEnumeration {
 	FLOAT_TRANSPARENT_BLACK(0),
 	INT_TRANSPARENT_BLACK(1),
 	FLOAT_OPAQUE_BLACK(2),
 	INT_OPAQUE_BLACK(3),
 	FLOAT_OPAQUE_WHITE(4),
 	INT_OPAQUE_WHITE(5);

	private final int value;

	private VkBorderColor(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
