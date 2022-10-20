package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkAttachmentDescriptionFlag implements IntegerEnumeration {
 	VK_ATTACHMENT_DESCRIPTION_MAY_ALIAS_BIT(1), 	
 	VK_ATTACHMENT_DESCRIPTION_FLAG_BITS_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkAttachmentDescriptionFlag(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
