package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkDebugUtilsMessageType implements IntegerEnumeration {
 	GENERAL(1),
 	VALIDATION(2),
 	PERFORMANCE(4);

	private final int value;

	private VkDebugUtilsMessageType(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
