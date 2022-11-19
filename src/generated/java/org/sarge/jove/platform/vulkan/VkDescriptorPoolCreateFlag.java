package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkDescriptorPoolCreateFlag implements IntEnum {
 	FREE_DESCRIPTOR_SET(1),
 	UPDATE_AFTER_BIND(2);

	private final int value;

	private VkDescriptorPoolCreateFlag(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
