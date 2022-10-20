package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkObjectEntryTypeNVX implements IntegerEnumeration {
 	VK_OBJECT_ENTRY_TYPE_DESCRIPTOR_SET_NVX(0), 	
 	VK_OBJECT_ENTRY_TYPE_PIPELINE_NVX(1), 	
 	VK_OBJECT_ENTRY_TYPE_INDEX_BUFFER_NVX(2), 	
 	VK_OBJECT_ENTRY_TYPE_VERTEX_BUFFER_NVX(3), 	
 	VK_OBJECT_ENTRY_TYPE_PUSH_CONSTANT_NVX(4), 	
 	VK_OBJECT_ENTRY_TYPE_BEGIN_RANGE_NVX(0), 	
 	VK_OBJECT_ENTRY_TYPE_END_RANGE_NVX(4), 	
 	VK_OBJECT_ENTRY_TYPE_RANGE_SIZE_NVX(5), 	
 	VK_OBJECT_ENTRY_TYPE_MAX_ENUM_NVX(2147483647); 	

	private final int value;
	
	private VkObjectEntryTypeNVX(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
