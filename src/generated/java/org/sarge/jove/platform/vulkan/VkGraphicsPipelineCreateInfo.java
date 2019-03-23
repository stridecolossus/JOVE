package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.VulkanLibrary.VulkanStructure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
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
	public static class ByValue extends VkGraphicsPipelineCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkGraphicsPipelineCreateInfo implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO;
	public Pointer pNext;
	public int flags;
	public int stageCount;
	public Pointer pStages;
	public VkPipelineVertexInputStateCreateInfo.ByReference pVertexInputState;
	public VkPipelineInputAssemblyStateCreateInfo.ByReference pInputAssemblyState;
	public VkPipelineTessellationStateCreateInfo.ByReference pTessellationState;
	public VkPipelineViewportStateCreateInfo.ByReference pViewportState;
	public VkPipelineRasterizationStateCreateInfo.ByReference pRasterizationState;
	public VkPipelineMultisampleStateCreateInfo.ByReference pMultisampleState;
	public VkPipelineDepthStencilStateCreateInfo.ByReference pDepthStencilState;
	public VkPipelineColorBlendStateCreateInfo.ByReference pColorBlendState;
	public VkPipelineDynamicStateCreateInfo.ByReference pDynamicState;
	public Pointer layout;
	public Pointer renderPass;
	public int subpass;
	public Pointer basePipelineHandle;
	public int basePipelineIndex;
}
