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
	"stage",
	"layout",
	"basePipelineHandle",
	"basePipelineIndex"
})
public class VkComputePipelineCreateInfo extends VulkanStructure {
	public VkStructureType sType = VkStructureType.COMPUTE_PIPELINE_CREATE_INFO;
	public Pointer pNext;
	public int flags;
	public VkPipelineShaderStageCreateInfo stage;
	public Pointer layout;
	public Pointer basePipelineHandle;
	public int basePipelineIndex;
}
