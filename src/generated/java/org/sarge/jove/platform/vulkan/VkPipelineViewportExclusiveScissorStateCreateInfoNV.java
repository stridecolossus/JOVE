package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

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
	"exclusiveScissorCount",
	"pExclusiveScissors"
})
public class VkPipelineViewportExclusiveScissorStateCreateInfoNV extends VulkanStructure {
	public static class ByValue extends VkPipelineViewportExclusiveScissorStateCreateInfoNV implements Structure.ByValue { }
	public static class ByReference extends VkPipelineViewportExclusiveScissorStateCreateInfoNV implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_EXCLUSIVE_SCISSOR_STATE_CREATE_INFO_NV;
	public Pointer pNext;
	public int exclusiveScissorCount;
	public Pointer pExclusiveScissors;
}
