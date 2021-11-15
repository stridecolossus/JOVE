package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkPolygonMode implements IntegerEnumeration {
 	FILL(0),
 	LINE(1),
 	POINT(2),
 	FILL_RECTANGLE_NV(1000153000);

	private final int value;

	private VkPolygonMode(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
