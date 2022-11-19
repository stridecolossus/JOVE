package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkAttachmentLoadOp implements IntEnum {
 	LOAD(0),
 	CLEAR(1),
 	DONT_CARE(2);

	private final int value;

	private VkAttachmentLoadOp(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
