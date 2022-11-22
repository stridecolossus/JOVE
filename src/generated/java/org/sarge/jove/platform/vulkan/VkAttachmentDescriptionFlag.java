package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkAttachmentDescriptionFlag implements IntEnum {
 	VK_ATTACHMENT_DESCRIPTION_MAY_ALIAS_BIT(1);

	private final int value;

	private VkAttachmentDescriptionFlag(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
