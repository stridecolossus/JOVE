package org.sarge.jove.platform.vulkan;

import java.lang.foreign.StructLayout;
import java.util.Collection;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.NativeStructure;
import org.sarge.jove.platform.vulkan.core.VulkanSemaphore;
import org.sarge.jove.platform.vulkan.render.Swapchain;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkPresentInfoKHR extends NativeStructure {
	public final VkStructureType sType = VkStructureType.PRESENT_INFO_KHR;
	public Handle pNext;
	public int waitSemaphoreCount;
	public Collection<VulkanSemaphore> pWaitSemaphores;
	public int swapchainCount;
	public Collection<Swapchain> pSwapchains;
	public int[] pImageIndices;
	public VkResult[] pResults;

	@Override
	protected StructLayout layout() {
		// TODO
		return null;
	}
}
