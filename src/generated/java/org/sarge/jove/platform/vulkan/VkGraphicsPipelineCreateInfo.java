package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sType",
	"pNext",
	"flags",
	"stageCount",
	"pStages",
	"pVertexInputState",
	"pInputAssemblyState",
	"pTessellationState",
	"pViewportState",
	"pRasterizationState",
	"pMultisampleState",
	"pDepthStencilState",
	"pColorBlendState",
	"pDynamicState",
	"layout",
	"renderPass",
	"subpass",
	"basePipelineHandle",
	"basePipelineIndex"
})
public class VkGraphicsPipelineCreateInfo extends VulkanStructure {
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO;
	public Pointer pNext;
	public int flags;
	public int stageCount;
	public VkPipelineShaderStageCreateInfo pStages;
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
