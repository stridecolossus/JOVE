package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkImageViewCreateFlag implements IntEnum {
 	CREATE_FRAGMENT_DENSITY_MAP_DYNAMIC_BIT_EXT(1);

	private final int value;

	private VkImageViewCreateFlag(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
