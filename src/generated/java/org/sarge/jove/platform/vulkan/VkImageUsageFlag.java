package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkImageUsageFlag implements IntegerEnumeration {
 	VK_IMAGE_USAGE_TRANSFER_SRC_BIT(1), 	
 	VK_IMAGE_USAGE_TRANSFER_DST_BIT(2), 	
 	VK_IMAGE_USAGE_SAMPLED_BIT(4), 	
 	VK_IMAGE_USAGE_STORAGE_BIT(8), 	
 	VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT(16), 	
 	VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT(32), 	
 	VK_IMAGE_USAGE_TRANSIENT_ATTACHMENT_BIT(64), 	
 	VK_IMAGE_USAGE_INPUT_ATTACHMENT_BIT(128), 	
 	VK_IMAGE_USAGE_SHADING_RATE_IMAGE_BIT_NV(256), 	
 	VK_IMAGE_USAGE_FRAGMENT_DENSITY_MAP_BIT_EXT(512), 	
 	VK_IMAGE_USAGE_FLAG_BITS_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkImageUsageFlag(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
