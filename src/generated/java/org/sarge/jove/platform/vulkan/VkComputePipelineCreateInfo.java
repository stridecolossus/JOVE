package org.sarge.jove.platform.vulkan;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.util.BitMask;

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
	public BitMask<VkPipelineCreateFlag> flags;
	public VkPipelineShaderStageCreateInfo stage;
	public Handle layout;
	public Handle basePipelineHandle;
	public int basePipelineIndex;
}
