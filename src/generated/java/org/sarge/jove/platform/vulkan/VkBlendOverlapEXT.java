package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkBlendOverlapEXT implements IntEnum {
	UNCORRELATED_EXT(0),
	DISJOINT_EXT(1),
	CONJOINT_EXT(2),
	MAX_ENUM_EXT(2147483647);

	private final int value;
	
	private VkBlendOverlapEXT(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
