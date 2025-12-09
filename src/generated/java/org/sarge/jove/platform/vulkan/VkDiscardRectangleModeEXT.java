package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkDiscardRectangleModeEXT implements IntEnum {
	INCLUSIVE_EXT(0),
	EXCLUSIVE_EXT(1),
	MAX_ENUM_EXT(2147483647);

	private final int value;
	
	private VkDiscardRectangleModeEXT(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
