package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkRasterizationOrderAMD implements IntEnum {
	STRICT_AMD(0),
	RELAXED_AMD(1),
	MAX_ENUM_AMD(2147483647);

	private final int value;
	
	private VkRasterizationOrderAMD(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
