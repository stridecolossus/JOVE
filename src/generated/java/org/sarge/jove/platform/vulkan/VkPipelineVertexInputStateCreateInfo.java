package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.VulkanStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkPipelineVertexInputStateCreateInfo extends VulkanStructure {
	public final VkStructureType sType = VkStructureType.PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO;
	public Handle pNext;
	public int flags;
	public int vertexBindingDescriptionCount;
	public VkVertexInputBindingDescription[] pVertexBindingDescriptions;
	public int vertexAttributeDescriptionCount;
	public VkVertexInputAttributeDescription[] pVertexAttributeDescriptions;
}
