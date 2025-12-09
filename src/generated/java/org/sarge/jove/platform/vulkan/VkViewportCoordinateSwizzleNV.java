package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkViewportCoordinateSwizzleNV implements IntEnum {
	POSITIVE_X_NV(0),
	NEGATIVE_X_NV(1),
	POSITIVE_Y_NV(2),
	NEGATIVE_Y_NV(3),
	POSITIVE_Z_NV(4),
	NEGATIVE_Z_NV(5),
	POSITIVE_W_NV(6),
	NEGATIVE_W_NV(7),
	MAX_ENUM_NV(2147483647);

	private final int value;
	
	private VkViewportCoordinateSwizzleNV(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
