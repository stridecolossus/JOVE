package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkPolygonMode implements IntEnum {
	FILL(0),
	LINE(1),
	POINT(2),
	FILL_RECTANGLE_NV(1000153000),
	MAX_ENUM(2147483647);

	private final int value;
	
	private VkPolygonMode(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
