package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkComponentTypeNV implements IntEnum {
	FLOAT16_NV(0),
	FLOAT32_NV(1),
	FLOAT64_NV(2),
	SINT8_NV(3),
	SINT16_NV(4),
	SINT32_NV(5),
	SINT64_NV(6),
	UINT8_NV(7),
	UINT16_NV(8),
	UINT32_NV(9),
	UINT64_NV(10),
	MAX_ENUM_NV(2147483647);

	private final int value;
	
	private VkComponentTypeNV(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
