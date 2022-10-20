package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkVertexInputRate implements IntegerEnumeration {
 	VERTEX(0),
 	INSTANCE(1);

	private final int value;

	private VkVertexInputRate(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
