package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkVendorId implements IntEnum {
	VIV(65537),
	VSI(65538),
	KAZAN(65539),
	CODEPLAY(65540),
	MESA(65541),
	MAX_ENUM(2147483647);

	private final int value;
	
	private VkVendorId(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
