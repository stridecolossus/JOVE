package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkSamplerAddressMode implements IntegerEnumeration {
 	REPEAT(0),
 	MIRRORED_REPEAT(1),
 	CLAMP_TO_EDGE(2),
 	CLAMP_TO_BORDER(3),
 	MIRROR_CLAMP_TO_EDGE(4);

	private final int value;

	private VkSamplerAddressMode(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
