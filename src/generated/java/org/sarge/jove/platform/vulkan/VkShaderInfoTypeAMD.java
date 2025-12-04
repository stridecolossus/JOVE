package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkShaderInfoTypeAMD implements IntEnum {
	STATISTICS_AMD(0),
	BINARY_AMD(1),
	DISASSEMBLY_AMD(2),
	MAX_ENUM_AMD(2147483647);

	private final int value;
	
	private VkShaderInfoTypeAMD(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
