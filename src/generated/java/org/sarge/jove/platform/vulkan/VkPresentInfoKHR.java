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
	"waitSemaphoreCount",
	"pWaitSemaphores",
	"swapchainCount",
	"pSwapchains",
	"pImageIndices",
	"pResults"
})
public class VkPresentInfoKHR extends VulkanStructure {
	public static class ByValue extends VkPresentInfoKHR implements Structure.ByValue { }
	public static class ByReference extends VkPresentInfoKHR implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_PRESENT_INFO_KHR;
	public Pointer pNext;
	public int waitSemaphoreCount;
	public Pointer pWaitSemaphores;
	public int swapchainCount;
	public Pointer pSwapchains;
	public Pointer pImageIndices;
	public Pointer pResults;
}