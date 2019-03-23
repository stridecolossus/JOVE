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
	"sampleOrderType",
	"customSampleOrderCount",
	"pCustomSampleOrders"
})
public class VkPipelineViewportCoarseSampleOrderStateCreateInfoNV extends Structure {
	public static class ByValue extends VkPipelineViewportCoarseSampleOrderStateCreateInfoNV implements Structure.ByValue { }
	public static class ByReference extends VkPipelineViewportCoarseSampleOrderStateCreateInfoNV implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_COARSE_SAMPLE_ORDER_STATE_CREATE_INFO_NV.value();
	public Pointer pNext;
	public int sampleOrderType;
	public int customSampleOrderCount;
	public VkCoarseSampleOrderCustomNV.ByReference pCustomSampleOrders;
}
