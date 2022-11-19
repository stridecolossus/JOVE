package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkDescriptorType implements IntEnum {
 	SAMPLER(0),
 	COMBINED_IMAGE_SAMPLER(1),
 	SAMPLED_IMAGE(2),
 	STORAGE_IMAGE(3),
 	UNIFORM_TEXEL_BUFFER(4),
 	STORAGE_TEXEL_BUFFER(5),
 	UNIFORM_BUFFER(6),
 	STORAGE_BUFFER(7),
 	UNIFORM_BUFFER_DYNAMIC(8),
 	STORAGE_BUFFER_DYNAMIC(9),
 	INPUT_ATTACHMENT(10),
 	INLINE_UNIFORM_BLOCK_EXT(1000138000),
 	ACCELERATION_STRUCTURE_NV(1000165000);

	private final int value;

	private VkDescriptorType(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
