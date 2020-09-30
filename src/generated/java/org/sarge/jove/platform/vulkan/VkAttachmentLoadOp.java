package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkAttachmentLoadOp implements IntegerEnumeration {
 	VK_ATTACHMENT_LOAD_OP_LOAD(0),
 	VK_ATTACHMENT_LOAD_OP_CLEAR(1),
 	VK_ATTACHMENT_LOAD_OP_DONT_CARE(2);

	private final int value;

	private VkAttachmentLoadOp(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
