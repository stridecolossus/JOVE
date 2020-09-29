package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkVertexInputRate implements IntegerEnumeration {
 	VK_VERTEX_INPUT_RATE_VERTEX(0), 	
 	VK_VERTEX_INPUT_RATE_INSTANCE(1), 	
 	VK_VERTEX_INPUT_RATE_BEGIN_RANGE(0), 	
 	VK_VERTEX_INPUT_RATE_END_RANGE(1), 	
 	VK_VERTEX_INPUT_RATE_RANGE_SIZE(2), 	
 	VK_VERTEX_INPUT_RATE_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkVertexInputRate(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
