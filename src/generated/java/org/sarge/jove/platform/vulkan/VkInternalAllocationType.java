package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkInternalAllocationType implements IntEnum {
	EXECUTABLE(0),
	MAX_ENUM(2147483647);

	private final int value;
	
	private VkInternalAllocationType(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
