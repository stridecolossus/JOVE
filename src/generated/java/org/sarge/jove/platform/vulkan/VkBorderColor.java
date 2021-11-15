package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkBorderColor implements IntegerEnumeration {
 	VK_BORDER_COLOR_FLOAT_TRANSPARENT_BLACK(0),
 	VK_BORDER_COLOR_INT_TRANSPARENT_BLACK(1),
 	VK_BORDER_COLOR_FLOAT_OPAQUE_BLACK(2),
 	VK_BORDER_COLOR_INT_OPAQUE_BLACK(3),
 	VK_BORDER_COLOR_FLOAT_OPAQUE_WHITE(4),
 	VK_BORDER_COLOR_INT_OPAQUE_WHITE(5);

	private final int value;

	private VkBorderColor(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
