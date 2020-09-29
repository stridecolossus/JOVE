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
	"shadingRateTexelSize",
	"shadingRatePaletteSize",
	"shadingRateMaxCoarseSamples"
})
public class VkPhysicalDeviceShadingRateImagePropertiesNV extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceShadingRateImagePropertiesNV implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceShadingRateImagePropertiesNV implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_SHADING_RATE_IMAGE_PROPERTIES_NV;
	public Pointer pNext;
	public VkExtent2D shadingRateTexelSize;
	public int shadingRatePaletteSize;
	public int shadingRateMaxCoarseSamples;
}
