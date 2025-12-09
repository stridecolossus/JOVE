package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkAttachmentStoreOp implements IntEnum {
	STORE(0),
	DONT_CARE(1),
	NONE_QCOM(1000301000),
	MAX_ENUM(2147483647);

	private final int value;
	
	private VkAttachmentStoreOp(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
