package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;
import com.sun.jna.Pointer;

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
public class VkPipelineRasterizationConservativeStateCreateInfoEXT extends Structure {
	public static class ByValue extends VkPipelineRasterizationConservativeStateCreateInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkPipelineRasterizationConservativeStateCreateInfoEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_CONSERVATIVE_STATE_CREATE_INFO_EXT.value();
	public Pointer pNext;
	public int flags;
	public int conservativeRasterizationMode;
	public float extraPrimitiveOverestimationSize;
}
