package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkStencilOp implements IntEnum {
	KEEP(0),
	ZERO(1),
	REPLACE(2),
	INCREMENT_AND_CLAMP(3),
	DECREMENT_AND_CLAMP(4),
	INVERT(5),
	INCREMENT_AND_WRAP(6),
	DECREMENT_AND_WRAP(7),
	MAX_ENUM(2147483647);

	private final int value;
	
	private VkStencilOp(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
