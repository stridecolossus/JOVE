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
	"conservativeRasterizationMode",
	"extraPrimitiveOverestimationSize"
})
public class VkPipelineRasterizationConservativeStateCreateInfoEXT extends VulkanStructure {
	public static class ByValue extends VkPipelineRasterizationConservativeStateCreateInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkPipelineRasterizationConservativeStateCreateInfoEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.PIPELINE_RASTERIZATION_CONSERVATIVE_STATE_CREATE_INFO_EXT;
	public Pointer pNext;
	public int flags;
	public VkConservativeRasterizationModeEXT conservativeRasterizationMode;
	public float extraPrimitiveOverestimationSize;
}
