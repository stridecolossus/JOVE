package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkSubpassContents implements IntegerEnumeration {
 	INLINE(0),
 	SECONDARY_COMMAND_BUFFERS(1);

	private final int value;

	private VkSubpassContents(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
