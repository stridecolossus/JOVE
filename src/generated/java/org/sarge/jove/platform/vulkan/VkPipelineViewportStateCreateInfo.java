package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.VulkanStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkPipelineViewportStateCreateInfo extends VulkanStructure {
	public VkStructureType sType = VkStructureType.PIPELINE_VIEWPORT_STATE_CREATE_INFO;
	public Handle pNext;
	public int flags;
	public int viewportCount;
	public VkViewport[] pViewports;
	public int scissorCount;
	public VkRect2D[] pScissors;
}
