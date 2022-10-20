package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkImageUsageFlag implements IntegerEnumeration {
 	TRANSFER_SRC(1),
 	TRANSFER_DST(2),
 	SAMPLED(4),
 	STORAGE(8),
 	COLOR_ATTACHMENT(16),
 	DEPTH_STENCIL_ATTACHMENT(32),
 	TRANSIENT_ATTACHMENT(64),
 	INPUT_ATTACHMENT(128),
 	SHADING_RATE_IMAGE_NV(256),
 	FRAGMENT_DENSITY_MAP_EXT(512);

	private final int value;

	private VkImageUsageFlag(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
