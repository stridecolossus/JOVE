package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sType",
	"pNext",
	"powerState"
})
public class VkDisplayPowerInfoEXT extends VulkanStructure {
	public VkStructureType sType = VkStructureType.DISPLAY_POWER_INFO_EXT;
	public Pointer pNext;
	public VkDisplayPowerStateEXT powerState;
}
