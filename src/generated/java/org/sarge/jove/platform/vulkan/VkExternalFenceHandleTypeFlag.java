package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkExternalFenceHandleTypeFlag implements IntegerEnumeration {
 	VK_EXTERNAL_FENCE_HANDLE_TYPE_OPAQUE_FD_BIT(1), 	
 	VK_EXTERNAL_FENCE_HANDLE_TYPE_OPAQUE_WIN32_BIT(2), 	
 	VK_EXTERNAL_FENCE_HANDLE_TYPE_OPAQUE_WIN32_KMT_BIT(4), 	
 	VK_EXTERNAL_FENCE_HANDLE_TYPE_SYNC_FD_BIT(8), 	
 	VK_EXTERNAL_FENCE_HANDLE_TYPE_OPAQUE_FD_BIT_KHR(1), 	
 	VK_EXTERNAL_FENCE_HANDLE_TYPE_OPAQUE_WIN32_BIT_KHR(2), 	
 	VK_EXTERNAL_FENCE_HANDLE_TYPE_OPAQUE_WIN32_KMT_BIT_KHR(4), 	
 	VK_EXTERNAL_FENCE_HANDLE_TYPE_SYNC_FD_BIT_KHR(8), 	
 	VK_EXTERNAL_FENCE_HANDLE_TYPE_FLAG_BITS_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkExternalFenceHandleTypeFlag(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
