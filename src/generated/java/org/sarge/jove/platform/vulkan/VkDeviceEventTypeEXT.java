package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkDeviceEventTypeEXT implements IntegerEnumeration {
 	VK_DEVICE_EVENT_TYPE_DISPLAY_HOTPLUG_EXT(0), 	
 	VK_DEVICE_EVENT_TYPE_BEGIN_RANGE_EXT(0), 	
 	VK_DEVICE_EVENT_TYPE_END_RANGE_EXT(0), 	
 	VK_DEVICE_EVENT_TYPE_RANGE_SIZE_EXT(1), 	
 	VK_DEVICE_EVENT_TYPE_MAX_ENUM_EXT(2147483647); 	

	private final int value;
	
	private VkDeviceEventTypeEXT(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
