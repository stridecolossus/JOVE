package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkDescriptorUpdateTemplateType implements IntEnum {
	DESCRIPTOR_SET(0),
	PUSH_DESCRIPTORS_KHR(1),
	DESCRIPTOR_SET_KHR(0),
	MAX_ENUM(2147483647);

	private final int value;
	
	private VkDescriptorUpdateTemplateType(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
