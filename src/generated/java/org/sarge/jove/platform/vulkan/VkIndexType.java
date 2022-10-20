package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkIndexType implements IntegerEnumeration {
 	UINT16(0),
 	UINT32(1),
 	NONE_NV(1000165000);

	private final int value;

	private VkIndexType(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
