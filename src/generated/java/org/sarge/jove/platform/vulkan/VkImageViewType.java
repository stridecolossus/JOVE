package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkImageViewType implements IntegerEnumeration {
 	ONE_D(0),
 	TWO_D(1),
 	THREE_D(2),
 	CUBE(3),
 	ONE_D_ARRAY(4),
 	TWO_D_ARRAY(5),
 	CUBE_ARRAY(6);

	private final int value;

	private VkImageViewType(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
