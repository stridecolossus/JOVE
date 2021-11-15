package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.core.VulkanLibrary.VulkanStructure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure.ByReference;
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
public class VkPresentInfoKHR extends VulkanStructure implements ByReference {
	public VkStructureType sType = VkStructureType.PRESENT_INFO_KHR;
	public Pointer pNext;
	public int waitSemaphoreCount;
	public Pointer pWaitSemaphores;
	public int swapchainCount;
	public Pointer pSwapchains;
	public Pointer pImageIndices;
	public Pointer pResults;
}
