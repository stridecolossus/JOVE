package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkQueryResultFlag implements IntegerEnumeration {
 	VK_QUERY_RESULT_64_BIT(1), 	
 	VK_QUERY_RESULT_WAIT_BIT(2), 	
 	VK_QUERY_RESULT_WITH_AVAILABILITY_BIT(4), 	
 	VK_QUERY_RESULT_PARTIAL_BIT(8), 	
 	VK_QUERY_RESULT_FLAG_BITS_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkQueryResultFlag(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
