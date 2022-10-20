package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.*;
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

	public VkStructureType sType = VkStructureType.PIPELINE_VIEWPORT_SHADING_RATE_IMAGE_STATE_CREATE_INFO_NV;
	public Pointer pNext;
	public boolean shadingRateImageEnable;
	public int viewportCount;
	public Pointer pShadingRatePalettes;
}
