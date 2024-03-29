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
	"cornerSampledImage"
})
public class VkPhysicalDeviceCornerSampledImageFeaturesNV extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceCornerSampledImageFeaturesNV implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceCornerSampledImageFeaturesNV implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.PHYSICAL_DEVICE_CORNER_SAMPLED_IMAGE_FEATURES_NV;
	public Pointer pNext;
	public boolean cornerSampledImage;
}
