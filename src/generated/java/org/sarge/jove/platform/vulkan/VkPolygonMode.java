package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkPolygonMode implements IntegerEnumeration {
 	VK_POLYGON_MODE_FILL(0),
 	VK_POLYGON_MODE_LINE(1),
 	VK_POLYGON_MODE_POINT(2),
 	VK_POLYGON_MODE_FILL_RECTANGLE_NV(1000153000);

	private final int value;

	private VkPolygonMode(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
