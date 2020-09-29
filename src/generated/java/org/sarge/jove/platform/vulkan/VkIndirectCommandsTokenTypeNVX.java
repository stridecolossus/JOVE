package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkIndirectCommandsTokenTypeNVX implements IntegerEnumeration {
 	VK_INDIRECT_COMMANDS_TOKEN_TYPE_PIPELINE_NVX(0), 	
 	VK_INDIRECT_COMMANDS_TOKEN_TYPE_DESCRIPTOR_SET_NVX(1), 	
 	VK_INDIRECT_COMMANDS_TOKEN_TYPE_INDEX_BUFFER_NVX(2), 	
 	VK_INDIRECT_COMMANDS_TOKEN_TYPE_VERTEX_BUFFER_NVX(3), 	
 	VK_INDIRECT_COMMANDS_TOKEN_TYPE_PUSH_CONSTANT_NVX(4), 	
 	VK_INDIRECT_COMMANDS_TOKEN_TYPE_DRAW_INDEXED_NVX(5), 	
 	VK_INDIRECT_COMMANDS_TOKEN_TYPE_DRAW_NVX(6), 	
 	VK_INDIRECT_COMMANDS_TOKEN_TYPE_DISPATCH_NVX(7), 	
 	VK_INDIRECT_COMMANDS_TOKEN_TYPE_BEGIN_RANGE_NVX(0), 	
 	VK_INDIRECT_COMMANDS_TOKEN_TYPE_END_RANGE_NVX(7), 	
 	VK_INDIRECT_COMMANDS_TOKEN_TYPE_RANGE_SIZE_NVX(8), 	
 	VK_INDIRECT_COMMANDS_TOKEN_TYPE_MAX_ENUM_NVX(2147483647); 	

	private final int value;
	
	private VkIndirectCommandsTokenTypeNVX(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
