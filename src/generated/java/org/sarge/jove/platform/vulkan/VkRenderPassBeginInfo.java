package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.VulkanStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkRenderPassBeginInfo extends VulkanStructure {
	public VkStructureType sType = VkStructureType.RENDER_PASS_BEGIN_INFO;
	public Handle pNext;
	public Handle renderPass;
	public Handle framebuffer;
	public VkRect2D renderArea;
	public int clearValueCount;
	public VkClearValue[] pClearValues;
}
