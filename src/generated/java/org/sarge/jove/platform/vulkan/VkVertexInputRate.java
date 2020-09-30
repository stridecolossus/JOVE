package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkVertexInputRate implements IntegerEnumeration {
 	VK_VERTEX_INPUT_RATE_VERTEX(0),
 	VK_VERTEX_INPUT_RATE_INSTANCE(1);

	private final int value;

	private VkVertexInputRate(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
