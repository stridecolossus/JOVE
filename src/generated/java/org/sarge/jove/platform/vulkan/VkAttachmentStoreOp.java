package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkAttachmentStoreOp implements IntegerEnumeration {
 	VK_ATTACHMENT_STORE_OP_STORE(0), 	
 	VK_ATTACHMENT_STORE_OP_DONT_CARE(1), 	
 	VK_ATTACHMENT_STORE_OP_BEGIN_RANGE(0), 	
 	VK_ATTACHMENT_STORE_OP_END_RANGE(1), 	
 	VK_ATTACHMENT_STORE_OP_RANGE_SIZE(2), 	
 	VK_ATTACHMENT_STORE_OP_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkAttachmentStoreOp(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
