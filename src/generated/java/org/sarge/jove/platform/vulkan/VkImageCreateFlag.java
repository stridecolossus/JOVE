package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkImageCreateFlag implements IntEnum {
 	SPARSE_BINDING(1),
 	SPARSE_RESIDENCY(2),
 	SPARSE_ALIASED(4),
 	MUTABLE_FORMAT(8),
 	CUBE_COMPATIBLE(16),
 	ALIAS(1024),
 	SPLIT_INSTANCE_BIND_REGIONS(64),
 	VK_IMAGE_CREATE_2D_ARRAY_COMPATIBLE(32),
 	BLOCK_TEXEL_VIEW_COMPATIBLE(128),
 	EXTENDED_USAGE(256),
 	PROTECTED(2048),
 	DISJOINT(512),
 	CORNER_SAMPLED_NV(8192),
 	SAMPLE_LOCATIONS_COMPATIBLE_DEPTH_EXT(4096),
 	SUBSAMPLED_EXT(16384),
 	SPLIT_INSTANCE_BIND_REGIONS_KHR(64),
 	VK_IMAGE_CREATE_2D_ARRAY_COMPATIBLE_KHR(32),
 	BLOCK_TEXEL_VIEW_COMPATIBLE_KHR(128),
 	EXTENDED_USAGE_KHR(256),
 	DISJOINT_KHR(512),
 	ALIAS_KHR(1024);

	private final int value;

	private VkImageCreateFlag(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
