package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkExternalMemoryHandleTypeFlagNV implements IntegerEnumeration {
 	VK_EXTERNAL_MEMORY_HANDLE_TYPE_OPAQUE_WIN32_BIT_NV(1), 	
 	VK_EXTERNAL_MEMORY_HANDLE_TYPE_OPAQUE_WIN32_KMT_BIT_NV(2), 	
 	VK_EXTERNAL_MEMORY_HANDLE_TYPE_D3D11_IMAGE_BIT_NV(4), 	
 	VK_EXTERNAL_MEMORY_HANDLE_TYPE_D3D11_IMAGE_KMT_BIT_NV(8), 	
 	VK_EXTERNAL_MEMORY_HANDLE_TYPE_FLAG_BITS_MAX_ENUM_NV(2147483647); 	

	private final int value;
	
	private VkExternalMemoryHandleTypeFlagNV(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
