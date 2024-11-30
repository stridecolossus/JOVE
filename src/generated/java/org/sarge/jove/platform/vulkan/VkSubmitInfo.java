package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.VulkanStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkSubmitInfo extends VulkanStructure {
	public VkStructureType sType = VkStructureType.SUBMIT_INFO;
	public Handle pNext;
	public int waitSemaphoreCount;
	public Handle pWaitSemaphores;
	public int pWaitDstStageMask;
	public int commandBufferCount;
	public Handle pCommandBuffers;
	public int signalSemaphoreCount;
	public Handle pSignalSemaphores;
}
