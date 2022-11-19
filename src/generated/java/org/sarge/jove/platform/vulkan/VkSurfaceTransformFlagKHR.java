package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkSurfaceTransformFlagKHR implements IntEnum {
 	IDENTITY_KHR(1),
 	ROTATE_90_KHR(2),
 	ROTATE_180_KHR(4),
 	ROTATE_270_KHR(8),
 	HORIZONTAL_MIRROR_KHR(16),
 	HORIZONTAL_MIRROR_ROTATE_90_KHR(32),
 	HORIZONTAL_MIRROR_ROTATE_180_KHR(64),
 	HORIZONTAL_MIRROR_ROTATE_270_KHR(128),
 	INHERIT_KHR(256);

	private final int value;

	private VkSurfaceTransformFlagKHR(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
