package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkImageLayout implements IntegerEnumeration {
 	UNDEFINED(0),
 	GENERAL(1),
 	COLOR_ATTACHMENT_OPTIMAL(2),
 	DEPTH_STENCIL_ATTACHMENT_OPTIMAL(3),
 	DEPTH_STENCIL_READ_ONLY_OPTIMAL(4),
 	SHADER_READ_ONLY_OPTIMAL(5),
 	TRANSFER_SRC_OPTIMAL(6),
 	TRANSFER_DST_OPTIMAL(7),
 	PREINITIALIZED(8),
 	DEPTH_READ_ONLY_STENCIL_ATTACHMENT_OPTIMAL(1000117000),
 	DEPTH_ATTACHMENT_STENCIL_READ_ONLY_OPTIMAL(1000117001),
 	PRESENT_SRC_KHR(1000001002),
 	SHARED_PRESENT_KHR(1000111000),
 	SHADING_RATE_OPTIMAL_NV(1000164003),
 	FRAGMENT_DENSITY_MAP_OPTIMAL_EXT(1000218000),
 	DEPTH_READ_ONLY_STENCIL_ATTACHMENT_OPTIMAL_KHR(1000117000),
 	DEPTH_ATTACHMENT_STENCIL_READ_ONLY_OPTIMAL_KHR(1000117001);

	private final int value;

	private VkImageLayout(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
