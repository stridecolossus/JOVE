package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.Handle;
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
	"pWaitDstStageMask",
	"commandBufferCount",
	"pCommandBuffers",
	"signalSemaphoreCount",
	"pSignalSemaphores"
})
public class VkSubmitInfo extends VulkanStructure {
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_SUBMIT_INFO;
	public Pointer pNext;
	public int waitSemaphoreCount;
	public Handle pWaitSemaphores;
	public Pointer pWaitDstStageMask;
	public int commandBufferCount;
	public Handle pCommandBuffers;
	public int signalSemaphoreCount;
	public Handle pSignalSemaphores;
}
