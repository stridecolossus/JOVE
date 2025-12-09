package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkSystemAllocationScope implements IntEnum {
	COMMAND(0),
	OBJECT(1),
	CACHE(2),
	DEVICE(3),
	INSTANCE(4),
	MAX_ENUM(2147483647);

	private final int value;
	
	private VkSystemAllocationScope(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
