package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkSparseImageFormatFlag implements IntEnum {
 	VK_SPARSE_IMAGE_FORMAT_SINGLE_MIPTAIL_BIT(1), 	
 	VK_SPARSE_IMAGE_FORMAT_ALIGNED_MIP_SIZE_BIT(2), 	
 	VK_SPARSE_IMAGE_FORMAT_NONSTANDARD_BLOCK_SIZE_BIT(4), 	
 	VK_SPARSE_IMAGE_FORMAT_FLAG_BITS_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkSparseImageFormatFlag(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
