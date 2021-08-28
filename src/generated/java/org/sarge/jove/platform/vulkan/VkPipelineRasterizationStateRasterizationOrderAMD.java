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
	"rasterizationOrder"
})
public class VkPipelineRasterizationStateRasterizationOrderAMD extends VulkanStructure {
	public static class ByValue extends VkPipelineRasterizationStateRasterizationOrderAMD implements Structure.ByValue { }
	public static class ByReference extends VkPipelineRasterizationStateRasterizationOrderAMD implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.PIPELINE_RASTERIZATION_STATE_RASTERIZATION_ORDER_AMD;
	public Pointer pNext;
	public VkRasterizationOrderAMD rasterizationOrder;
}
