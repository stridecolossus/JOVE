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
	"shadingRateImageEnable",
	"viewportCount",
	"pShadingRatePalettes"
})
public class VkPipelineViewportShadingRateImageStateCreateInfoNV extends Structure {
	public static class ByValue extends VkPipelineViewportShadingRateImageStateCreateInfoNV implements Structure.ByValue { }
	public static class ByReference extends VkPipelineViewportShadingRateImageStateCreateInfoNV implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_SHADING_RATE_IMAGE_STATE_CREATE_INFO_NV.value();
	public Pointer pNext;
	public boolean shadingRateImageEnable;
	public int viewportCount;
	public VkShadingRatePaletteNV.ByReference pShadingRatePalettes;
}
