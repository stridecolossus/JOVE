package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkIndexType implements IntegerEnumeration {
 	VK_INDEX_TYPE_UINT16(0), 	
 	VK_INDEX_TYPE_UINT32(1), 	
 	VK_INDEX_TYPE_NONE_NV(1000165000), 	
 	VK_INDEX_TYPE_BEGIN_RANGE(0), 	
 	VK_INDEX_TYPE_END_RANGE(1), 	
 	VK_INDEX_TYPE_RANGE_SIZE(2), 	
 	VK_INDEX_TYPE_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkIndexType(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
