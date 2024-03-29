package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkSamplerMipmapMode implements IntEnum {
 	NEAREST(0),
 	LINEAR(1);

	private final int value;

	private VkSamplerMipmapMode(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
