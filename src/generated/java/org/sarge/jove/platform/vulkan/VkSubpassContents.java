package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkSubpassContents implements IntEnum {
	INLINE(0),
	SECONDARY_COMMAND_BUFFERS(1),
	MAX_ENUM(2147483647);

	private final int value;
	
	private VkSubpassContents(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
