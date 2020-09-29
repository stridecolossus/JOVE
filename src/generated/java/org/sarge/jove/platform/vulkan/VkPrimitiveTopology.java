package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkPrimitiveTopology implements IntegerEnumeration {
 	VK_PRIMITIVE_TOPOLOGY_POINT_LIST(0), 	
 	VK_PRIMITIVE_TOPOLOGY_LINE_LIST(1), 	
 	VK_PRIMITIVE_TOPOLOGY_LINE_STRIP(2), 	
 	VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST(3), 	
 	VK_PRIMITIVE_TOPOLOGY_TRIANGLE_STRIP(4), 	
 	VK_PRIMITIVE_TOPOLOGY_TRIANGLE_FAN(5), 	
 	VK_PRIMITIVE_TOPOLOGY_LINE_LIST_WITH_ADJACENCY(6), 	
 	VK_PRIMITIVE_TOPOLOGY_LINE_STRIP_WITH_ADJACENCY(7), 	
 	VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST_WITH_ADJACENCY(8), 	
 	VK_PRIMITIVE_TOPOLOGY_TRIANGLE_STRIP_WITH_ADJACENCY(9), 	
 	VK_PRIMITIVE_TOPOLOGY_PATCH_LIST(10), 	
 	VK_PRIMITIVE_TOPOLOGY_BEGIN_RANGE(0), 	
 	VK_PRIMITIVE_TOPOLOGY_END_RANGE(10), 	
 	VK_PRIMITIVE_TOPOLOGY_RANGE_SIZE(11), 	
 	VK_PRIMITIVE_TOPOLOGY_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkPrimitiveTopology(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
