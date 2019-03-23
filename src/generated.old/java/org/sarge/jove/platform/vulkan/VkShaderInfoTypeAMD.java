package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkShaderInfoTypeAMD implements IntegerEnumeration {
 	VK_SHADER_INFO_TYPE_STATISTICS_AMD(0), 	
 	VK_SHADER_INFO_TYPE_BINARY_AMD(1), 	
 	VK_SHADER_INFO_TYPE_DISASSEMBLY_AMD(2), 	
 	VK_SHADER_INFO_TYPE_BEGIN_RANGE_AMD(0), 	
 	VK_SHADER_INFO_TYPE_END_RANGE_AMD(2), 	
 	VK_SHADER_INFO_TYPE_RANGE_SIZE_AMD(3), 	
 	VK_SHADER_INFO_TYPE_MAX_ENUM_AMD(2147483647); 	

	private final int value;
	
	private VkShaderInfoTypeAMD(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
