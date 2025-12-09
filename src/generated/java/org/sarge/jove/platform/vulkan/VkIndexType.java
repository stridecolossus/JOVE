package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkIndexType implements IntEnum {
	UINT16(0),
	UINT32(1),
	NONE_KHR(1000165000),
	UINT8_EXT(1000265000),
	NONE_NV(1000165000),
	MAX_ENUM(2147483647);

	private final int value;
	
	private VkIndexType(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
