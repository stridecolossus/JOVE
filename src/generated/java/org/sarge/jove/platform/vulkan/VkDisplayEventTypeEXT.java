package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkDisplayEventTypeEXT implements IntEnum {
	FIRST_PIXEL_OUT_EXT(0),
	MAX_ENUM_EXT(2147483647);

	private final int value;
	
	private VkDisplayEventTypeEXT(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
