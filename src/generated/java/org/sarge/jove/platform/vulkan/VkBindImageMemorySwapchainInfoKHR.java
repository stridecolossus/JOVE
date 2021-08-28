package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

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
	"swapchain",
	"imageIndex"
})
public class VkBindImageMemorySwapchainInfoKHR extends VulkanStructure {
	public static class ByValue extends VkBindImageMemorySwapchainInfoKHR implements Structure.ByValue { }
	public static class ByReference extends VkBindImageMemorySwapchainInfoKHR implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.BIND_IMAGE_MEMORY_SWAPCHAIN_INFO_KHR;
	public Pointer pNext;
	public long swapchain;
	public int imageIndex;
}
