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
	"shadingRateImage",
	"shadingRateCoarseSampleOrder"
})
public class VkPhysicalDeviceShadingRateImageFeaturesNV extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceShadingRateImageFeaturesNV implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceShadingRateImageFeaturesNV implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.PHYSICAL_DEVICE_SHADING_RATE_IMAGE_FEATURES_NV;
	public Pointer pNext;
	public boolean shadingRateImage;
	public boolean shadingRateCoarseSampleOrder;
}
