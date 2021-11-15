package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkExternalMemoryFeatureFlagBitsNV implements IntegerEnumeration {
 	VK_EXTERNAL_MEMORY_FEATURE_DEDICATED_ONLY_BIT_NV(1), 	
 	VK_EXTERNAL_MEMORY_FEATURE_EXPORTABLE_BIT_NV(2), 	
 	VK_EXTERNAL_MEMORY_FEATURE_IMPORTABLE_BIT_NV(4), 	
 	VK_EXTERNAL_MEMORY_FEATURE_FLAG_BITS_MAX_ENUM_NV(2147483647); 	

	private final int value;
	
	private VkExternalMemoryFeatureFlagBitsNV(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
