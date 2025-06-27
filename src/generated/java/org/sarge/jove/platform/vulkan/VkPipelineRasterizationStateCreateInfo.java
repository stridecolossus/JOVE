package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.VulkanStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkPipelineRasterizationStateCreateInfo extends VulkanStructure {
	public final VkStructureType sType = VkStructureType.PIPELINE_RASTERIZATION_STATE_CREATE_INFO;
	public Handle pNext;
	public int flags;
	public boolean depthClampEnable;
	public boolean rasterizerDiscardEnable;
	public VkPolygonMode polygonMode;
	public VkCullMode cullMode;
	public VkFrontFace frontFace;
	public boolean depthBiasEnable;
	public float depthBiasConstantFactor;
	public float depthBiasClamp;
	public float depthBiasSlopeFactor;
	public float lineWidth;
}
