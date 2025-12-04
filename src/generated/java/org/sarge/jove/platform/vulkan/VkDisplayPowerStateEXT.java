package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkDisplayPowerStateEXT implements IntEnum {
	OFF_EXT(0),
	SUSPEND_EXT(1),
	ON_EXT(2),
	MAX_ENUM_EXT(2147483647);

	private final int value;
	
	private VkDisplayPowerStateEXT(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
