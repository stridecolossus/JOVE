package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkImageType implements IntEnum {
	TYPE_1D(0),
	TYPE_2D(1),
	TYPE_3D(2),
	MAX_ENUM(2147483647);

	private final int value;
	
	private VkImageType(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
