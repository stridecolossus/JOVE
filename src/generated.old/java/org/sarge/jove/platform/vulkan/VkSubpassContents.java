package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkSubpassContents implements IntegerEnumeration {
 	VK_SUBPASS_CONTENTS_INLINE(0), 	
 	VK_SUBPASS_CONTENTS_SECONDARY_COMMAND_BUFFERS(1), 	
 	VK_SUBPASS_CONTENTS_BEGIN_RANGE(0), 	
 	VK_SUBPASS_CONTENTS_END_RANGE(1), 	
 	VK_SUBPASS_CONTENTS_RANGE_SIZE(2), 	
 	VK_SUBPASS_CONTENTS_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkSubpassContents(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
