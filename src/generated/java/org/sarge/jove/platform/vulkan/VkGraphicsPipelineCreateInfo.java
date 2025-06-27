package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.util.EnumMask;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkGraphicsPipelineCreateInfo extends VulkanStructure {
	public VkStructureType sType = VkStructureType.GRAPHICS_PIPELINE_CREATE_INFO;
	public Handle pNext;
	public EnumMask<VkPipelineCreateFlag> flags;
	public int stageCount;
	public VkPipelineShaderStageCreateInfo[] pStages;
	public VkPipelineVertexInputStateCreateInfo pVertexInputState;
	public VkPipelineInputAssemblyStateCreateInfo pInputAssemblyState;
	public VkPipelineTessellationStateCreateInfo pTessellationState;
	public VkPipelineViewportStateCreateInfo pViewportState;
	public VkPipelineRasterizationStateCreateInfo pRasterizationState;
	public VkPipelineMultisampleStateCreateInfo pMultisampleState;
	public VkPipelineDepthStencilStateCreateInfo pDepthStencilState;
	public VkPipelineColorBlendStateCreateInfo pColorBlendState;
	public VkPipelineDynamicStateCreateInfo pDynamicState;
	public Handle layout;
	public Handle renderPass;
	public int subpass;
	public Handle basePipelineHandle;
	public int basePipelineIndex;
}
