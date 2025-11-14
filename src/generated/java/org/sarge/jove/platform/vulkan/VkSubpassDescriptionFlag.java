package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkSubpassDescriptionFlag implements IntEnum {
 	PER_VIEW_ATTRIBUTES_BIT_NVX(1),
 	PER_VIEW_POSITION_X_ONLY_BIT_NVX(2);

	private final int value;

	private VkSubpassDescriptionFlag(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
