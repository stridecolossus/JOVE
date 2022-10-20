package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkPeerMemoryFeatureFlag implements IntegerEnumeration {
 	VK_PEER_MEMORY_FEATURE_COPY_SRC_BIT(1), 	
 	VK_PEER_MEMORY_FEATURE_COPY_DST_BIT(2), 	
 	VK_PEER_MEMORY_FEATURE_GENERIC_SRC_BIT(4), 	
 	VK_PEER_MEMORY_FEATURE_GENERIC_DST_BIT(8), 	
 	VK_PEER_MEMORY_FEATURE_COPY_SRC_BIT_KHR(1), 	
 	VK_PEER_MEMORY_FEATURE_COPY_DST_BIT_KHR(2), 	
 	VK_PEER_MEMORY_FEATURE_GENERIC_SRC_BIT_KHR(4), 	
 	VK_PEER_MEMORY_FEATURE_GENERIC_DST_BIT_KHR(8), 	
 	VK_PEER_MEMORY_FEATURE_FLAG_BITS_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkPeerMemoryFeatureFlag(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
