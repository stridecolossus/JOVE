package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkTessellationDomainOrigin implements IntEnum {
	UPPER_LEFT(0),
	LOWER_LEFT(1),
	UPPER_LEFT_KHR(0),
	LOWER_LEFT_KHR(1),
	MAX_ENUM(2147483647);

	private final int value;
	
	private VkTessellationDomainOrigin(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
