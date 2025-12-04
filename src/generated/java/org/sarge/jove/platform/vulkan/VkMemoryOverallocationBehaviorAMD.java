package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkMemoryOverallocationBehaviorAMD implements IntEnum {
	DEFAULT_AMD(0),
	ALLOWED_AMD(1),
	DISALLOWED_AMD(2),
	MAX_ENUM_AMD(2147483647);

	private final int value;
	
	private VkMemoryOverallocationBehaviorAMD(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
