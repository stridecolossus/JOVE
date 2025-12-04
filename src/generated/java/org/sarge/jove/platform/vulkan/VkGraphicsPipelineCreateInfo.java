package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.*;

import java.lang.foreign.*;

import org.sarge.jove.foreign.NativeStructure;
import org.sarge.jove.common.Handle;
import org.sarge.jove.util.EnumMask;
import org.sarge.jove.platform.vulkan.*;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkGraphicsPipelineCreateInfo implements NativeStructure {
	public VkStructureType sType;
	public Handle pNext;
	public EnumMask<VkPipelineCreateFlags> flags;
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

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("sType"),
			PADDING,
			POINTER.withName("pNext"),
			JAVA_INT.withName("flags"),
			JAVA_INT.withName("stageCount"),
			POINTER.withName("pStages"),
			POINTER.withName("pVertexInputState"),
			POINTER.withName("pInputAssemblyState"),
			POINTER.withName("pTessellationState"),
			POINTER.withName("pViewportState"),
			POINTER.withName("pRasterizationState"),
			POINTER.withName("pMultisampleState"),
			POINTER.withName("pDepthStencilState"),
			POINTER.withName("pColorBlendState"),
			POINTER.withName("pDynamicState"),
			POINTER.withName("layout"),
			POINTER.withName("renderPass"),
			JAVA_INT.withName("subpass"),
			PADDING,
			POINTER.withName("basePipelineHandle"),
			JAVA_INT.withName("basePipelineIndex")
		);
	}
}
