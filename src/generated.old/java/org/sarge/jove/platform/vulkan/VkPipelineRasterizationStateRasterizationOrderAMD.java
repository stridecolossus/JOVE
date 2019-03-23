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
	"rasterizationOrder"
})
public class VkPipelineRasterizationStateRasterizationOrderAMD extends Structure {
	public static class ByValue extends VkPipelineRasterizationStateRasterizationOrderAMD implements Structure.ByValue { }
	public static class ByReference extends VkPipelineRasterizationStateRasterizationOrderAMD implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_RASTERIZATION_ORDER_AMD.value();
	public Pointer pNext;
	public int rasterizationOrder;
}
