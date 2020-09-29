package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkExternalFenceFeatureFlag implements IntegerEnumeration {
 	VK_EXTERNAL_FENCE_FEATURE_EXPORTABLE_BIT(1), 	
 	VK_EXTERNAL_FENCE_FEATURE_IMPORTABLE_BIT(2), 	
 	VK_EXTERNAL_FENCE_FEATURE_EXPORTABLE_BIT_KHR(1), 	
 	VK_EXTERNAL_FENCE_FEATURE_IMPORTABLE_BIT_KHR(2), 	
 	VK_EXTERNAL_FENCE_FEATURE_FLAG_BITS_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkExternalFenceFeatureFlag(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
