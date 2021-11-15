package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkDebugUtilsMessageSeverity implements IntegerEnumeration {
 	VERBOSE(1),
 	INFO(16),
 	WARNING(256),
 	ERROR(4096);

	private final int value;

	private VkDebugUtilsMessageSeverity(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
