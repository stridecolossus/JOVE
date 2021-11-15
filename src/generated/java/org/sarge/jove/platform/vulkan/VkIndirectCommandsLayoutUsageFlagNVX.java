package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkIndirectCommandsLayoutUsageFlagNVX implements IntegerEnumeration {
 	VK_INDIRECT_COMMANDS_LAYOUT_USAGE_UNORDERED_SEQUENCES_BIT_NVX(1), 	
 	VK_INDIRECT_COMMANDS_LAYOUT_USAGE_SPARSE_SEQUENCES_BIT_NVX(2), 	
 	VK_INDIRECT_COMMANDS_LAYOUT_USAGE_EMPTY_EXECUTIONS_BIT_NVX(4), 	
 	VK_INDIRECT_COMMANDS_LAYOUT_USAGE_INDEXED_SEQUENCES_BIT_NVX(8), 	
 	VK_INDIRECT_COMMANDS_LAYOUT_USAGE_FLAG_BITS_MAX_ENUM_NVX(2147483647); 	

	private final int value;
	
	private VkIndirectCommandsLayoutUsageFlagNVX(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
