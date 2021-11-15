package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkImageAspect implements IntegerEnumeration {
 	COLOR(1),
 	DEPTH(2),
 	STENCIL(4),
 	METADATA(8),
 	PLANE_0(16),
 	PLANE_1(32),
 	PLANE_2(64),
 	MEMORY_PLANE_0_EXT(128),
 	MEMORY_PLANE_1_EXT(256),
 	MEMORY_PLANE_2_EXT(512),
 	MEMORY_PLANE_3_EXT(1024),
 	PLANE_0_KHR(16),
 	PLANE_1_KHR(32),
 	PLANE_2_KHR(64);

	private final int value;

	private VkImageAspect(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
