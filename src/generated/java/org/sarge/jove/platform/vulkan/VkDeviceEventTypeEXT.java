package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkDeviceEventTypeEXT implements IntEnum {
	DISPLAY_HOTPLUG_EXT(0),
	MAX_ENUM_EXT(2147483647);

	private final int value;
	
	private VkDeviceEventTypeEXT(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
