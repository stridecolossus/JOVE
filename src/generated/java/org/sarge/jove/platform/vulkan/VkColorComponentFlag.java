package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkColorComponentFlag implements IntegerEnumeration {
 	VK_COLOR_COMPONENT_R_BIT(1),
 	VK_COLOR_COMPONENT_G_BIT(2),
 	VK_COLOR_COMPONENT_B_BIT(4),
 	VK_COLOR_COMPONENT_A_BIT(8),
 	VK_COLOR_COMPONENT_FLAG_BITS_MAX_ENUM(2147483647);

	private final int value;

	private VkColorComponentFlag(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
