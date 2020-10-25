package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sType",
	"pNext",
	"waitSemaphoreCount",
	"pWaitSemaphores",
	"swapchainCount",
	"pSwapchains",
	"pImageIndices",
	"pResults"
})
public class VkPresentInfoKHR extends VulkanStructure {
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_PRESENT_INFO_KHR;
	public Pointer pNext;
	public int waitSemaphoreCount;
	public Pointer pWaitSemaphores;
	public int swapchainCount;
	public Pointer pSwapchains;
	public Pointer pImageIndices;
	public Pointer pResults;
}
