package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

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
	"setLayoutCount",
	"pSetLayouts",
	"pushConstantRangeCount",
	"pPushConstantRanges"
})
public class VkPipelineLayoutCreateInfo extends VulkanStructure {
	public VkStructureType sType = VkStructureType.PIPELINE_LAYOUT_CREATE_INFO;
	public Pointer pNext;
	public int flags;
	public int setLayoutCount;
	public Pointer pSetLayouts;
	public int pushConstantRangeCount;
	public VkPushConstantRange pPushConstantRanges;
}
