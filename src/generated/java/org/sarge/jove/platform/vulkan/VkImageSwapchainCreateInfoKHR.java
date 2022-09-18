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
	"swapchain"
})
public class VkImageSwapchainCreateInfoKHR extends VulkanStructure {
	public VkStructureType sType = VkStructureType.IMAGE_SWAPCHAIN_CREATE_INFO_KHR;
	public Pointer pNext;
	public long swapchain;
}
