package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkBlendFactor implements IntEnum {
 	ZERO(0),
 	ONE(1),
 	SRC_COLOR(2),
 	ONE_MINUS_SRC_COLOR(3),
 	DST_COLOR(4),
 	ONE_MINUS_DST_COLOR(5),
 	SRC_ALPHA(6),
 	ONE_MINUS_SRC_ALPHA(7),
 	DST_ALPHA(8),
 	ONE_MINUS_DST_ALPHA(9),
 	CONSTANT_COLOR(10),
 	ONE_MINUS_CONSTANT_COLOR(11),
 	CONSTANT_ALPHA(12),
 	ONE_MINUS_CONSTANT_ALPHA(13),
 	SRC_ALPHA_SATURATE(14),
 	SRC1_COLOR(15),
 	ONE_MINUS_SRC1_COLOR(16),
 	SRC1_ALPHA(17),
 	ONE_MINUS_SRC1_ALPHA(18);

	private final int value;

	private VkBlendFactor(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
