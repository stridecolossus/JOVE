package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkPrimitiveTopology implements IntEnum {
 	POINT_LIST(0),
 	LINE_LIST(1),
 	LINE_STRIP(2),
 	TRIANGLE_LIST(3),
 	TRIANGLE_STRIP(4),
 	TRIANGLE_FAN(5),
 	LINE_LIST_WITH_ADJACENCY(6),
 	LINE_STRIP_WITH_ADJACENCY(7),
 	TRIANGLE_LIST_WITH_ADJACENCY(8),
 	TRIANGLE_STRIP_WITH_ADJACENCY(9),
 	PATCH_LIST(10);

	private final int value;

	private VkPrimitiveTopology(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
