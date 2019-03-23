package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkAttachmentLoadOp implements IntegerEnumeration {
 	VK_ATTACHMENT_LOAD_OP_LOAD(0), 	
 	VK_ATTACHMENT_LOAD_OP_CLEAR(1), 	
 	VK_ATTACHMENT_LOAD_OP_DONT_CARE(2), 	
 	VK_ATTACHMENT_LOAD_OP_BEGIN_RANGE(0), 	
 	VK_ATTACHMENT_LOAD_OP_END_RANGE(2), 	
 	VK_ATTACHMENT_LOAD_OP_RANGE_SIZE(3), 	
 	VK_ATTACHMENT_LOAD_OP_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkAttachmentLoadOp(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
