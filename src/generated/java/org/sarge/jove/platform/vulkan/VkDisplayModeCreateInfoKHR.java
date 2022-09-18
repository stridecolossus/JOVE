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
	"flags",
	"parameters"
})
public class VkDisplayModeCreateInfoKHR extends VulkanStructure {
	public VkStructureType sType = VkStructureType.DISPLAY_MODE_CREATE_INFO_KHR;
	public Pointer pNext;
	public int flags;
	public VkDisplayModeParametersKHR parameters;
}
