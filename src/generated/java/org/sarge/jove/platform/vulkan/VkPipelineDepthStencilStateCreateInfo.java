package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.VulkanStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkPipelineDepthStencilStateCreateInfo extends VulkanStructure {
	public final VkStructureType sType = VkStructureType.PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO;
	public Handle pNext;
	public int flags;
	public boolean depthTestEnable;
	public boolean depthWriteEnable;
	public VkCompareOp depthCompareOp;
	public boolean depthBoundsTestEnable;
	public boolean stencilTestEnable;
	public VkStencilOpState front;
	public VkStencilOpState back;
	public float minDepthBounds;
	public float maxDepthBounds;
}
