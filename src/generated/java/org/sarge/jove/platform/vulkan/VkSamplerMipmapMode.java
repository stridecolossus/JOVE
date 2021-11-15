package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkSamplerMipmapMode implements IntegerEnumeration {
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
