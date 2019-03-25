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
	"pWaitDstStageMask",
	"commandBufferCount",
	"pCommandBuffers",
	"signalSemaphoreCount",
	"pSignalSemaphores"
})
public class VkSubmitInfo extends VulkanStructure {
	public static class ByValue extends VkSubmitInfo implements Structure.ByValue { }
	public static class ByReference extends VkSubmitInfo implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_SUBMIT_INFO;
	public Pointer pNext;
	public int waitSemaphoreCount;
	public Pointer pWaitSemaphores;
	public Pointer pWaitDstStageMask;
	public int commandBufferCount;
	public Pointer pCommandBuffers;
	public int signalSemaphoreCount;
	public Pointer pSignalSemaphores;
}