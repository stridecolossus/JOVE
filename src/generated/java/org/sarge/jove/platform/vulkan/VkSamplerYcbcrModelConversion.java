package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkSamplerYcbcrModelConversion implements IntEnum {
	RGB_IDENTITY(0),
	YCBCR_IDENTITY(1),
	YCBCR_709(2),
	YCBCR_601(3),
	YCBCR_2020(4),
	RGB_IDENTITY_KHR(0),
	YCBCR_IDENTITY_KHR(1),
	YCBCR_709_KHR(2),
	YCBCR_601_KHR(3),
	YCBCR_2020_KHR(4),
	MAX_ENUM(2147483647);

	private final int value;
	
	private VkSamplerYcbcrModelConversion(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
