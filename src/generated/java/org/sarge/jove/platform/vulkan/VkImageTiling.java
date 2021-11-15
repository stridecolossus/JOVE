package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkImageTiling implements IntegerEnumeration {
 	OPTIMAL(0),
 	LINEAR(1),
 	DRM_FORMAT_MODIFIER_EXT(1000158000);

	private final int value;

	private VkImageTiling(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
