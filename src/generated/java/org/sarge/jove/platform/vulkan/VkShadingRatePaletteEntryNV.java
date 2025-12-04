package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkShadingRatePaletteEntryNV implements IntEnum {
	NO_INVOCATIONS_NV(0),
	ENTRY_16_INVOCATIONS_PER_PIXEL_NV(1),
	ENTRY_8_INVOCATIONS_PER_PIXEL_NV(2),
	ENTRY_4_INVOCATIONS_PER_PIXEL_NV(3),
	ENTRY_2_INVOCATIONS_PER_PIXEL_NV(4),
	ENTRY_1_INVOCATION_PER_PIXEL_NV(5),
	ENTRY_1_INVOCATION_PER_2X1_PIXELS_NV(6),
	ENTRY_1_INVOCATION_PER_1X2_PIXELS_NV(7),
	ENTRY_1_INVOCATION_PER_2X2_PIXELS_NV(8),
	ENTRY_1_INVOCATION_PER_4X2_PIXELS_NV(9),
	ENTRY_1_INVOCATION_PER_2X4_PIXELS_NV(10),
	ENTRY_1_INVOCATION_PER_4X4_PIXELS_NV(11),
	MAX_ENUM_NV(2147483647);

	private final int value;
	
	private VkShadingRatePaletteEntryNV(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
