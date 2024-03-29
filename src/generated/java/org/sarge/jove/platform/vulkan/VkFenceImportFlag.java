package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkFenceImportFlag implements IntEnum {
 	VK_FENCE_IMPORT_TEMPORARY_BIT(1), 	
 	VK_FENCE_IMPORT_TEMPORARY_BIT_KHR(1), 	
 	VK_FENCE_IMPORT_FLAG_BITS_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkFenceImportFlag(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
