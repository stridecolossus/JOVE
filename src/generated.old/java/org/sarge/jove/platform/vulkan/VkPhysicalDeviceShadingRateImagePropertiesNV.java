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
	"shadingRateTexelSize",
	"shadingRatePaletteSize",
	"shadingRateMaxCoarseSamples"
})
public class VkPhysicalDeviceShadingRateImagePropertiesNV extends Structure {
	public static class ByValue extends VkPhysicalDeviceShadingRateImagePropertiesNV implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceShadingRateImagePropertiesNV implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_SHADING_RATE_IMAGE_PROPERTIES_NV.value();
	public Pointer pNext;
	public VkExtent2D shadingRateTexelSize;
	public int shadingRatePaletteSize;
	public int shadingRateMaxCoarseSamples;
}
