package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkCommandPoolCreateFlag implements IntegerEnumeration {
 	TRANSIENT(1),
 	RESET_COMMAND_BUFFER(2),
 	PROTECTED(4);

	private final int value;

	private VkCommandPoolCreateFlag(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
