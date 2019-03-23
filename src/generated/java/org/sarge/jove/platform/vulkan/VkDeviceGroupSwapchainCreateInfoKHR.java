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
	"modes"
})
public class VkDeviceGroupSwapchainCreateInfoKHR extends VulkanStructure {
	public static class ByValue extends VkDeviceGroupSwapchainCreateInfoKHR implements Structure.ByValue { }
	public static class ByReference extends VkDeviceGroupSwapchainCreateInfoKHR implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_DEVICE_GROUP_SWAPCHAIN_CREATE_INFO_KHR;
	public Pointer pNext;
	public VkDeviceGroupPresentModeFlagsKHR modes;
}
