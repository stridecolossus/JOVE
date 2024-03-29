package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkSharingMode implements IntEnum {
 	EXCLUSIVE(0),
 	CONCURRENT(1);

	private final int value;

	private VkSharingMode(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
