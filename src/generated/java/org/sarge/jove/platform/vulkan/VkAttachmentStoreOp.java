package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkAttachmentStoreOp implements IntegerEnumeration {
 	STORE(0),
 	DONT_CARE(1);

	private final int value;

	private VkAttachmentStoreOp(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
