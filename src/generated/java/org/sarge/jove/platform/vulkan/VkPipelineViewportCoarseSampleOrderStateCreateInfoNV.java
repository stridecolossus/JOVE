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
	"sampleOrderType",
	"customSampleOrderCount",
	"pCustomSampleOrders"
})
public class VkPipelineViewportCoarseSampleOrderStateCreateInfoNV extends VulkanStructure {
	public static class ByValue extends VkPipelineViewportCoarseSampleOrderStateCreateInfoNV implements Structure.ByValue { }
	public static class ByReference extends VkPipelineViewportCoarseSampleOrderStateCreateInfoNV implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_COARSE_SAMPLE_ORDER_STATE_CREATE_INFO_NV;
	public Pointer pNext;
	public VkCoarseSampleOrderTypeNV sampleOrderType;
	public int customSampleOrderCount;
	public Pointer pCustomSampleOrders;
}
