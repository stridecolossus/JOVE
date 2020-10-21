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
	"flags",
	"rasterizationStream"
})
public class VkPipelineRasterizationStateStreamCreateInfoEXT extends VulkanStructure {
	public static class ByValue extends VkPipelineRasterizationStateStreamCreateInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkPipelineRasterizationStateStreamCreateInfoEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_STREAM_CREATE_INFO_EXT;
	public Pointer pNext;
	public int flags;
	public int rasterizationStream;
}
