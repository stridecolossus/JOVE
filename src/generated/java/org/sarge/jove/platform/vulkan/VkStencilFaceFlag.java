package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkStencilFaceFlag implements IntEnum {
 	FRONT(1),
 	BACK(2),
 	FRONT_AND_BACK(3);

	private final int value;

	private VkStencilFaceFlag(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
