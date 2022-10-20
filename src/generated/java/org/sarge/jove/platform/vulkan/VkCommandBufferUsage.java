package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkCommandBufferUsage implements IntegerEnumeration {
 	ONE_TIME_SUBMIT(1),
 	RENDER_PASS_CONTINUE(2),
 	SIMULTANEOUS_USE(4);

	private final int value;

	private VkCommandBufferUsage(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
