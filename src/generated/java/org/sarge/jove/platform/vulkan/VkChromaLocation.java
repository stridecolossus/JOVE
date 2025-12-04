package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkChromaLocation implements IntEnum {
	COSITED_EVEN(0),
	MIDPOINT(1),
	COSITED_EVEN_KHR(0),
	MIDPOINT_KHR(1),
	MAX_ENUM(2147483647);

	private final int value;
	
	private VkChromaLocation(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
