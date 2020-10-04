package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkImageTiling implements IntegerEnumeration {
 	VK_IMAGE_TILING_OPTIMAL(0),
 	VK_IMAGE_TILING_LINEAR(1),
 	VK_IMAGE_TILING_DRM_FORMAT_MODIFIER_EXT(1000158000);

	private final int value;

	private VkImageTiling(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
