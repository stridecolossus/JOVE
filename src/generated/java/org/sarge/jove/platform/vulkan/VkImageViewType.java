package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkImageViewType implements IntEnum {
	TYPE_1D(0),
	TYPE_2D(1),
	TYPE_3D(2),
	CUBE(3),
	TYPE_1D_ARRAY(4),
	TYPE_2D_ARRAY(5),
	CUBE_ARRAY(6),
	MAX_ENUM(2147483647);

	private final int value;
	
	private VkImageViewType(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
