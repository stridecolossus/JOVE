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
	"stage",
	"layout",
	"basePipelineHandle",
	"basePipelineIndex"
})
public class VkComputePipelineCreateInfo extends VulkanStructure {
	public static class ByValue extends VkComputePipelineCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkComputePipelineCreateInfo implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_COMPUTE_PIPELINE_CREATE_INFO;
	public Pointer pNext;
	public int flags;
	public VkPipelineShaderStageCreateInfo stage;
	public Pointer layout;
	public Pointer basePipelineHandle;
	public int basePipelineIndex;
}
