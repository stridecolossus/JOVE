package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkColorComponentFlag implements IntegerEnumeration {
 	R(1),
 	G(2),
 	B(4),
 	A(8);

	private final int value;

	private VkColorComponentFlag(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
