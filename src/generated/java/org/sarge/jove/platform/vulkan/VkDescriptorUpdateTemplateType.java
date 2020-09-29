package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkDescriptorUpdateTemplateType implements IntegerEnumeration {
 	VK_DESCRIPTOR_UPDATE_TEMPLATE_TYPE_DESCRIPTOR_SET(0), 	
 	VK_DESCRIPTOR_UPDATE_TEMPLATE_TYPE_PUSH_DESCRIPTORS_KHR(1), 	
 	VK_DESCRIPTOR_UPDATE_TEMPLATE_TYPE_DESCRIPTOR_SET_KHR(0), 	
 	VK_DESCRIPTOR_UPDATE_TEMPLATE_TYPE_BEGIN_RANGE(0), 	
 	VK_DESCRIPTOR_UPDATE_TEMPLATE_TYPE_END_RANGE(0), 	
 	VK_DESCRIPTOR_UPDATE_TEMPLATE_TYPE_RANGE_SIZE(1), 	
 	VK_DESCRIPTOR_UPDATE_TEMPLATE_TYPE_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkDescriptorUpdateTemplateType(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
