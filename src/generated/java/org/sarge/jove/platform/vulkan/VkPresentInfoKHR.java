package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.VulkanStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkPresentInfoKHR extends VulkanStructure {
	public final VkStructureType sType = VkStructureType.PRESENT_INFO_KHR;
	public Handle pNext;
	public int waitSemaphoreCount;
	public Handle[] pWaitSemaphores;
	public int swapchainCount;
	public Handle[] pSwapchains;
	public int[] pImageIndices;
	public VkResult[] pResults;
}
