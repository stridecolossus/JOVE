package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkComponentTypeNV implements IntEnum {
 	VK_COMPONENT_TYPE_FLOAT16_NV(0), 	
 	VK_COMPONENT_TYPE_FLOAT32_NV(1), 	
 	VK_COMPONENT_TYPE_FLOAT64_NV(2), 	
 	VK_COMPONENT_TYPE_SINT8_NV(3), 	
 	VK_COMPONENT_TYPE_SINT16_NV(4), 	
 	VK_COMPONENT_TYPE_SINT32_NV(5), 	
 	VK_COMPONENT_TYPE_SINT64_NV(6), 	
 	VK_COMPONENT_TYPE_UINT8_NV(7), 	
 	VK_COMPONENT_TYPE_UINT16_NV(8), 	
 	VK_COMPONENT_TYPE_UINT32_NV(9), 	
 	VK_COMPONENT_TYPE_UINT64_NV(10), 	
 	VK_COMPONENT_TYPE_BEGIN_RANGE_NV(0), 	
 	VK_COMPONENT_TYPE_END_RANGE_NV(10), 	
 	VK_COMPONENT_TYPE_RANGE_SIZE_NV(11), 	
 	VK_COMPONENT_TYPE_MAX_ENUM_NV(2147483647); 	

	private final int value;
	
	private VkComponentTypeNV(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
