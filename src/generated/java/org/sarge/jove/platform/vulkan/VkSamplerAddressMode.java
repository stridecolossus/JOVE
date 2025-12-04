package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkSamplerAddressMode implements IntEnum {
	REPEAT(0),
	MIRRORED_REPEAT(1),
	CLAMP_TO_EDGE(2),
	CLAMP_TO_BORDER(3),
	MIRROR_CLAMP_TO_EDGE(4),
	MIRROR_CLAMP_TO_EDGE_KHR(4),
	MAX_ENUM(2147483647);

	private final int value;
	
	private VkSamplerAddressMode(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
