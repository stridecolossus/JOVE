package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkPointClippingBehavior implements IntEnum {
	ALL_CLIP_PLANES(0),
	USER_CLIP_PLANES_ONLY(1),
	ALL_CLIP_PLANES_KHR(0),
	USER_CLIP_PLANES_ONLY_KHR(1),
	MAX_ENUM(2147483647);

	private final int value;
	
	private VkPointClippingBehavior(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
