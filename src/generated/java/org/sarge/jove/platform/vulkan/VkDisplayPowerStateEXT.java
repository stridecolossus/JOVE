package org.sarge.jove.platform.vulkan;

import org.sarge.jove.util.IntEnum;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkDisplayPowerStateEXT implements IntEnum {
 	VK_DISPLAY_POWER_STATE_OFF_EXT(0), 	
 	VK_DISPLAY_POWER_STATE_SUSPEND_EXT(1), 	
 	VK_DISPLAY_POWER_STATE_ON_EXT(2), 	
 	VK_DISPLAY_POWER_STATE_BEGIN_RANGE_EXT(0), 	
 	VK_DISPLAY_POWER_STATE_END_RANGE_EXT(2), 	
 	VK_DISPLAY_POWER_STATE_RANGE_SIZE_EXT(3), 	
 	VK_DISPLAY_POWER_STATE_MAX_ENUM_EXT(2147483647); 	

	private final int value;
	
	private VkDisplayPowerStateEXT(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
