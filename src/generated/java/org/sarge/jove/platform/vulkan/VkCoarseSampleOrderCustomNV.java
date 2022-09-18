package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"shadingRate",
	"sampleCount",
	"sampleLocationCount",
	"pSampleLocations"
})
public class VkCoarseSampleOrderCustomNV extends VulkanStructure {
	public VkShadingRatePaletteEntryNV shadingRate;
	public int sampleCount;
	public int sampleLocationCount;
	public Pointer pSampleLocations;
}
