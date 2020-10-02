package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkSharingMode implements IntegerEnumeration {
 	VK_SHARING_MODE_EXCLUSIVE(0),
 	VK_SHARING_MODE_CONCURRENT(1);

	private final int value;

	private VkSharingMode(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
