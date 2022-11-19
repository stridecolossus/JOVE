package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkDependencyFlag implements IntEnum {
 	BY_REGION(1),
 	DEVICE_GROUP(4),
 	VIEW_LOCAL(2),
 	VIEW_LOCAL_KHR(2),
 	DEVICE_GROUP_KHR(4);

	private final int value;

	private VkDependencyFlag(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
