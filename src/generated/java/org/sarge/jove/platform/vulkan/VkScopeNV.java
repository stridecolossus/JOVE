package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkScopeNV implements IntEnum {
	DEVICE_NV(1),
	WORKGROUP_NV(2),
	SUBGROUP_NV(3),
	QUEUE_FAMILY_NV(5),
	MAX_ENUM_NV(2147483647);

	private final int value;
	
	private VkScopeNV(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
