package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkImageType implements IntegerEnumeration {
 	IMAGE_TYPE_1D(0),
 	IMAGE_TYPE_2D(1),
 	IMAGE_TYPE_3D(2);

	private final int value;

	private VkImageType(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
