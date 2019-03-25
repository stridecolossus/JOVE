package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.VulkanLibrary.VulkanStructure;
import com.sun.jna.Pointer;

import com.sun.jna.Structure;
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
	public static class ByValue extends VkDisplayModeCreateInfoKHR implements Structure.ByValue { }
	public static class ByReference extends VkDisplayModeCreateInfoKHR implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_DISPLAY_MODE_CREATE_INFO_KHR;
	public Pointer pNext;
	public int flags;
	public VkDisplayModeParametersKHR parameters;
}