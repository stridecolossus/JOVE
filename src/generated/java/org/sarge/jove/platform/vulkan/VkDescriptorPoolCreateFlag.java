package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkDescriptorPoolCreateFlag implements IntegerEnumeration {
 	VK_DESCRIPTOR_POOL_CREATE_FREE_DESCRIPTOR_SET_BIT(1),
 	VK_DESCRIPTOR_POOL_CREATE_UPDATE_AFTER_BIND_BIT_EXT(2);

	private final int value;

	private VkDescriptorPoolCreateFlag(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
