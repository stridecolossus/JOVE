package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.VulkanStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkPipelineLayoutCreateInfo extends VulkanStructure {
	public VkStructureType sType = VkStructureType.PIPELINE_LAYOUT_CREATE_INFO;
	public Handle pNext;
	public int flags;
	public int setLayoutCount;
	public Handle[] pSetLayouts;
	public int pushConstantRangeCount;
	public VkPushConstantRange pPushConstantRanges;
}
