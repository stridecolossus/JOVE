package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.VulkanLibrary.VulkanStructure;
import org.sarge.jove.platform.vulkan.VulkanBoolean;
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
	"shadingRateImageEnable",
	"viewportCount",
	"pShadingRatePalettes"
})
public class VkPipelineViewportShadingRateImageStateCreateInfoNV extends VulkanStructure {
	public static class ByValue extends VkPipelineViewportShadingRateImageStateCreateInfoNV implements Structure.ByValue { }
	public static class ByReference extends VkPipelineViewportShadingRateImageStateCreateInfoNV implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_SHADING_RATE_IMAGE_STATE_CREATE_INFO_NV;
	public Pointer pNext;
	public VulkanBoolean shadingRateImageEnable;
	public int viewportCount;
	public Pointer pShadingRatePalettes;
}
