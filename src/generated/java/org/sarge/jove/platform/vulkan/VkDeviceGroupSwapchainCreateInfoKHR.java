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
	"modes"
})
public class VkDeviceGroupSwapchainCreateInfoKHR extends VulkanStructure {
	public VkStructureType sType = VkStructureType.DEVICE_GROUP_SWAPCHAIN_CREATE_INFO_KHR;
	public Pointer pNext;
	public VkDeviceGroupPresentModeFlagKHR modes;
}
