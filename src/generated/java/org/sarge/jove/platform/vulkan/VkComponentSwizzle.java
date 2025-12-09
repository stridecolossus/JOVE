package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkComponentSwizzle implements IntEnum {
	IDENTITY(0),
	ZERO(1),
	ONE(2),
	R(3),
	G(4),
	B(5),
	A(6),
	MAX_ENUM(2147483647);

	private final int value;
	
	private VkComponentSwizzle(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
