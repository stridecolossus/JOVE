package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.VulkanStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkPipelineColorBlendStateCreateInfo extends VulkanStructure {
	public final VkStructureType sType = VkStructureType.PIPELINE_COLOR_BLEND_STATE_CREATE_INFO;
	public Handle pNext;
	public int flags;
	public boolean logicOpEnable;
	public VkLogicOp logicOp;
	public int attachmentCount;
	public VkPipelineColorBlendAttachmentState[] pAttachments;
	public float[] blendConstants = new float[4];
}
